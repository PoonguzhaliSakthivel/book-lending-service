package com.library.lending.exception;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) {
        super("A member with email " + email + " already exists");
    }
}
