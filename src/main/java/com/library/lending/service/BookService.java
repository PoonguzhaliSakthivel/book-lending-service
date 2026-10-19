package com.library.lending.service;

import com.library.lending.domain.Book;
import com.library.lending.dto.BookRequest;
import com.library.lending.dto.BookUpdateRequest;
import com.library.lending.exception.BookNotFoundException;
import com.library.lending.exception.DuplicateIsbnException;
import com.library.lending.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            log.warn("Rejected createBook: ISBN {} already exists", request.isbn());
            throw new DuplicateIsbnException(request.isbn());
        }
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .totalCopies(request.totalCopies())
                .availableCopies(request.totalCopies())
                .build();
        Book saved = bookRepository.save(book);
        log.info("Created book id={} isbn={} totalCopies={}", saved.getId(), saved.getIsbn(), saved.getTotalCopies());
        return saved;
    }

    public Book updateBook(Long id, BookUpdateRequest request) {
        Book book = getBook(id);
        int onLoan = book.getTotalCopies() - book.getAvailableCopies();
        int totalCopies = request.totalCopies();
        if (totalCopies < onLoan) {
            log.warn("Rejected updateBook id={}: requested totalCopies={} is below copies on loan={}",
                    id, totalCopies, onLoan);
            throw new IllegalArgumentException(
                    "Cannot set totalCopies below the number of copies currently on loan (" + onLoan + ")");
        }
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setAvailableCopies(totalCopies - onLoan);
        book.setTotalCopies(totalCopies);
        Book saved = bookRepository.save(book);
        log.info("Updated book id={} totalCopies={} availableCopies={}",
                saved.getId(), saved.getTotalCopies(), saved.getAvailableCopies());
        return saved;
    }

    public void deleteBook(Long id) {
        Book book = getBook(id);
        bookRepository.delete(book);
        log.info("Deleted book id={} isbn={}", id, book.getIsbn());
    }

    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        log.debug("Fetching book id={}", id);
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String query, Pageable pageable) {
        String q = query == null ? "" : query;
        log.debug("Searching books query='{}' page={} size={}", q, pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q, pageable);
    }
}
