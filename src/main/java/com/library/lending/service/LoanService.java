package com.library.lending.service;

import com.library.lending.config.LoanProperties;
import com.library.lending.domain.Book;
import com.library.lending.domain.Loan;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.exception.BookNotFoundException;
import com.library.lending.exception.LoanAlreadyReturnedException;
import com.library.lending.exception.LoanNotFoundException;
import com.library.lending.exception.MaxActiveLoansExceededException;
import com.library.lending.exception.MemberNotFoundException;
import com.library.lending.exception.NoAvailableCopiesException;
import com.library.lending.exception.OverdueLoanExistsException;
import com.library.lending.repository.BookRepository;
import com.library.lending.repository.LoanRepository;
import com.library.lending.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanProperties loanProperties;
    private final Clock clock;

    public LoanService(LoanRepository loanRepository,
                        BookRepository bookRepository,
                        MemberRepository memberRepository,
                        LoanProperties loanProperties,
                        Clock clock) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanProperties = loanProperties;
        this.clock = clock;
    }

    public Loan borrow(BorrowRequest request) {
        Long bookId = request.bookId();
        Long memberId = request.memberId();
        Book book = bookRepository.findByIdForUpdate(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        LocalDateTime now = LocalDateTime.now(clock);

        if (!book.hasAvailableCopy()) {
            log.warn("Rejected borrow: book id={} has no available copies", bookId);
            throw new NoAvailableCopiesException(bookId);
        }
        if (loanRepository.existsOverdueLoanForMember(memberId, now)) {
            log.warn("Rejected borrow: member id={} has an overdue loan", memberId);
            throw new OverdueLoanExistsException(memberId);
        }
        long activeLoans = loanRepository.countActiveLoansByMember(memberId);
        if (activeLoans >= loanProperties.getMaxActiveLoansPerMember()) {
            log.warn("Rejected borrow: member id={} already has {} active loans (max={})",
                    memberId, activeLoans, loanProperties.getMaxActiveLoansPerMember());
            throw new MaxActiveLoansExceededException(memberId, loanProperties.getMaxActiveLoansPerMember());
        }

        book.decrementAvailableCopies();
        bookRepository.save(book);

        Loan loan = Loan.builder()
                .bookId(bookId)
                .memberId(memberId)
                .borrowedAt(now)
                .dueDate(now.plusDays(loanProperties.getLoanDurationDays()))
                .build();
        loan = loanRepository.save(loan);

        log.info("Borrowed book id={} by member id={} loanId={} dueDate={}",
                bookId, memberId, loan.getId(), loan.getDueDate());
        return loan;
    }

    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        if (!loan.isActive()) {
            log.warn("Rejected returnLoan: loan id={} was already returned", loanId);
            throw new LoanAlreadyReturnedException(loanId);
        }

        loan.setReturnedAt(LocalDateTime.now(clock));
        loan = loanRepository.save(loan);

        Long bookId = loan.getBookId();
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        book.incrementAvailableCopies();
        bookRepository.save(book);

        log.info("Returned loan id={} bookId={} memberId={}", loanId, bookId, loan.getMemberId());
        return loan;
    }

    @Transactional(readOnly = true)
    public Loan getLoan(Long id) {
        log.debug("Fetching loan id={}", id);
        return loanRepository.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Loan> listAll(Pageable pageable) {
        log.debug("Listing all loans page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return loanRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Loan> listActive(Pageable pageable) {
        log.debug("Listing active loans page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return loanRepository.findByReturnedAtIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Loan> listOverdue(Pageable pageable) {
        log.debug("Listing overdue loans page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return loanRepository.findOverdueLoans(LocalDateTime.now(clock), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Loan> listForMember(Long memberId, Pageable pageable) {
        memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));
        log.debug("Listing loans for member id={} page={} size={}",
                memberId, pageable.getPageNumber(), pageable.getPageSize());
        return loanRepository.findByMemberId(memberId, pageable);
    }
}
