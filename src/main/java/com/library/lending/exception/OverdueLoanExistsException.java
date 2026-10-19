package com.library.lending.exception;

public class OverdueLoanExistsException extends ConflictException {
    public OverdueLoanExistsException(Long memberId) {
        super("Member " + memberId + " has at least one overdue loan and cannot borrow more books");
    }
}
