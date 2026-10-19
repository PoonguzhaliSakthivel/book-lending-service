package com.library.lending.dto;

import com.library.lending.domain.Book;

public record BookResponse(
        Long id, String title, String author, String isbn, int totalCopies, int availableCopies) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getTotalCopies(), book.getAvailableCopies());
    }
}
