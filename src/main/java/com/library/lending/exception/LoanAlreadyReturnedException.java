package com.library.lending.exception;

public class LoanAlreadyReturnedException extends ConflictException {
    public LoanAlreadyReturnedException(Long loanId) {
        super("Loan " + loanId + " has already been returned");
    }
}
