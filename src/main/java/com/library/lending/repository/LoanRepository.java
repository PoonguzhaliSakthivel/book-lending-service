package com.library.lending.repository;

import com.library.lending.domain.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Page<Loan> findByReturnedAtIsNull(Pageable pageable);

    Page<Loan> findByMemberId(Long memberId, Pageable pageable);

    @Query("select l from Loan l where l.memberId = :memberId and l.returnedAt is null")
    List<Loan> findActiveLoansByMember(@Param("memberId") Long memberId);

    @Query("select count(l) from Loan l where l.memberId = :memberId and l.returnedAt is null")
    long countActiveLoansByMember(@Param("memberId") Long memberId);

    @Query("""
            select count(l) > 0 from Loan l
            where l.memberId = :memberId and l.returnedAt is null and l.dueDate < :now
            """)
    boolean existsOverdueLoanForMember(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Query("select l from Loan l where l.returnedAt is null and l.dueDate < :now")
    Page<Loan> findOverdueLoans(@Param("now") LocalDateTime now, Pageable pageable);
}
