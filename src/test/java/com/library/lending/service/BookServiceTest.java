package com.library.lending.service;

import com.library.lending.domain.Book;
import com.library.lending.dto.BookRequest;
import com.library.lending.exception.BookNotFoundException;
import com.library.lending.exception.DuplicateIsbnException;
import com.library.lending.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    @Test
    void createBook_succeeds_withFullAvailability() {
        when(bookRepository.existsByIsbn("111")).thenReturn(false);
        when(bookRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.createBook(new BookRequest("Title", "Author", "111", 5));

        assertThat(book.getTotalCopies()).isEqualTo(5);
        assertThat(book.getAvailableCopies()).isEqualTo(5);
    }

    @Test
    void createBook_rejectsDuplicateIsbn() {
        when(bookRepository.existsByIsbn("111")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(new BookRequest("Title", "Author", "111", 5)))
                .isInstanceOf(DuplicateIsbnException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBook_throwsWhenMissing() {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBook(42L))
                .isInstanceOf(BookNotFoundException.class);
    }
}
