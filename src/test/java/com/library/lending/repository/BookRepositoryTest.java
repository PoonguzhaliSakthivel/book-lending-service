package com.library.lending.repository;

import com.library.lending.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void existsByIsbn_detectsDuplicates() {
        bookRepository.save(Book.builder().title("T").author("A").isbn("ISBN-1").totalCopies(2).availableCopies(2).build());

        assertThat(bookRepository.existsByIsbn("ISBN-1")).isTrue();
        assertThat(bookRepository.existsByIsbn("ISBN-2")).isFalse();
    }

    @Test
    void search_matchesTitleOrAuthorCaseInsensitively() {
        bookRepository.save(Book.builder().title("Clean Code").author("Robert Martin").isbn("ISBN-2").totalCopies(1).availableCopies(1).build());
        bookRepository.save(Book.builder().title("Refactoring").author("Martin Fowler").isbn("ISBN-3").totalCopies(1).availableCopies(1).build());

        var byTitle = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                "clean", "clean", PageRequest.of(0, 10));
        var byAuthor = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                "martin", "martin", PageRequest.of(0, 10));

        assertThat(byTitle.getContent()).hasSize(1);
        assertThat(byAuthor.getContent()).hasSize(2);
    }
}
