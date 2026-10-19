package com.library.lending.exception;

public class BookNotFoundException extends NotFoundException {
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
}
