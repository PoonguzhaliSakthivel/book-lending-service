package com.library.lending.exception;

public class DuplicateIsbnException extends ConflictException {
    public DuplicateIsbnException(String isbn) {
        super("A book with ISBN " + isbn + " already exists");
    }
}
