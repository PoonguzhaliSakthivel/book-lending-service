package com.library.lending.repository;

import com.library.lending.domain.Book;
import com.library.lending.domain.Loan;
import com.library.lending.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void overdueAndActiveQueries_reflectLoanState() {
        Book book = bookRepository.save(Book.builder().title("T").author("A").isbn("ISBN-9").totalCopies(2).availableCopies(1).build());
        Member member = memberRepository.save(Member.builder().name("N").email("n@x.com").build());

        LocalDateTime now = LocalDateTime.now();
        loanRepository.save(Loan.builder()
                .bookId(book.getId()).memberId(member.getId())
                .borrowedAt(now.minusDays(20)).dueDate(now.minusDays(6))
                .build());

        assertThat(loanRepository.countActiveLoansByMember(member.getId())).isEqualTo(1);
        assertThat(loanRepository.existsOverdueLoanForMember(member.getId(), now)).isTrue();
        assertThat(loanRepository.findActiveLoansByMember(member.getId())).hasSize(1);
    }
}
