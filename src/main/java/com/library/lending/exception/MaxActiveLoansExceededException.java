package com.library.lending.exception;

public class MaxActiveLoansExceededException extends ConflictException {
    public MaxActiveLoansExceededException(Long memberId, int max) {
        super("Member " + memberId + " already has the maximum of " + max + " active loans");
    }
}
