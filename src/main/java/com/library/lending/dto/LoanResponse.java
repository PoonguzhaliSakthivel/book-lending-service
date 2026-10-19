package com.library.lending.dto;

import com.library.lending.domain.Loan;

import java.time.LocalDateTime;

public record LoanResponse(
        Long id, Long bookId, Long memberId,
        LocalDateTime borrowedAt, LocalDateTime dueDate, LocalDateTime returnedAt) {

    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(), loan.getBookId(), loan.getMemberId(),
                loan.getBorrowedAt(), loan.getDueDate(), loan.getReturnedAt());
    }
}
