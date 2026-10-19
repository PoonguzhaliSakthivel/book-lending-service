package com.library.lending.exception;

public class NoAvailableCopiesException extends ConflictException {
    public NoAvailableCopiesException(Long bookId) {
        super("No available copies for book id: " + bookId);
    }
}
