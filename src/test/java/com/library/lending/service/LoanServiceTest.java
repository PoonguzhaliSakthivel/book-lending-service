package com.library.lending.service;

import com.library.lending.config.LoanProperties;
import com.library.lending.domain.Book;
import com.library.lending.domain.Loan;
import com.library.lending.domain.Member;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.exception.LoanAlreadyReturnedException;
import com.library.lending.exception.MaxActiveLoansExceededException;
import com.library.lending.exception.NoAvailableCopiesException;
import com.library.lending.exception.OverdueLoanExistsException;
import com.library.lending.repository.BookRepository;
import com.library.lending.repository.LoanRepository;
import com.library.lending.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;

    private LoanProperties loanProperties;
    private LoanService loanService;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        loanProperties = new LoanProperties();
        loanProperties.setMaxActiveLoansPerMember(3);
        loanProperties.setLoanDurationDays(14);
        loanService = new LoanService(loanRepository, bookRepository, memberRepository, loanProperties, fixedClock);
    }

    private Book sampleBook(int available) {
        return Book.builder().id(1L).title("t").author("a").isbn("123").totalCopies(3).availableCopies(available).build();
    }

    private Member sampleMember() {
        return Member.builder().id(2L).name("n").email("e@x.com").build();
    }

    @Test
    void borrow_succeeds_andCalculatesDueDateFromConfig() {
        Book book = sampleBook(1);
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(sampleMember()));
        when(loanRepository.existsOverdueLoanForMember(eq(2L), any())).thenReturn(false);
        when(loanRepository.countActiveLoansByMember(2L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan loan = loanService.borrow(new BorrowRequest(1L, 2L));

        assertThat(loan.getBorrowedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        assertThat(loan.getDueDate()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
        assertThat(book.getAvailableCopies()).isEqualTo(0);
    }

    @Test
    void borrow_rejected_whenNoAvailableCopies() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleBook(0)));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(sampleMember()));

        assertThatThrownBy(() -> loanService.borrow(new BorrowRequest(1L, 2L)))
                .isInstanceOf(NoAvailableCopiesException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_rejected_whenMemberHasOverdueLoan() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleBook(1)));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(sampleMember()));
        when(loanRepository.existsOverdueLoanForMember(eq(2L), any())).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrow(new BorrowRequest(1L, 2L)))
                .isInstanceOf(OverdueLoanExistsException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_rejected_whenMaxActiveLoansReached() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleBook(1)));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(sampleMember()));
        when(loanRepository.existsOverdueLoanForMember(eq(2L), any())).thenReturn(false);
        when(loanRepository.countActiveLoansByMember(2L)).thenReturn(3L);

        assertThatThrownBy(() -> loanService.borrow(new BorrowRequest(1L, 2L)))
                .isInstanceOf(MaxActiveLoansExceededException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_succeeds_andIncrementsAvailableCopies() {
        Loan activeLoan = Loan.builder().id(5L).bookId(1L).memberId(2L)
                .borrowedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .dueDate(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();
        Book book = sampleBook(0);

        when(loanRepository.findById(5L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

        Loan returned = loanService.returnLoan(5L);

        assertThat(returned.getReturnedAt()).isNotNull();
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void returnLoan_rejected_whenAlreadyReturned() {
        Loan returnedLoan = Loan.builder().id(5L).bookId(1L).memberId(2L)
                .borrowedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .dueDate(LocalDateTime.of(2026, 1, 15, 10, 0))
                .returnedAt(LocalDateTime.of(2026, 1, 10, 10, 0))
                .build();

        when(loanRepository.findById(5L)).thenReturn(Optional.of(returnedLoan));

        assertThatThrownBy(() -> loanService.returnLoan(5L))
                .isInstanceOf(LoanAlreadyReturnedException.class);
    }
}
