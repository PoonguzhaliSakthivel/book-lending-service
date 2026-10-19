package com.library.lending.exception;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(Long id) {
        super("Member not found with id: " + id);
    }
}
