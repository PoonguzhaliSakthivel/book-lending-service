package com.library.lending.exception;

public class LoanNotFoundException extends NotFoundException {
    public LoanNotFoundException(Long id) {
        super("Loan not found with id: " + id);
    }
}
