package com.library.lending.controller;

import com.library.lending.domain.Book;
import com.library.lending.dto.BookRequest;
import com.library.lending.dto.BookResponse;
import com.library.lending.dto.BookUpdateRequest;
import com.library.lending.dto.PageResponse;
import com.library.lending.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Catalog management")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Add a new book to the catalog")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Update a book's details (ISBN is immutable)")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        Book book = bookService.updateBook(id, request);
        return BookResponse.from(book);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Remove a book from the catalog")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by id")
    public BookResponse getById(@PathVariable Long id) {
        return BookResponse.from(bookService.getBook(id));
    }

    @GetMapping
    @Operation(summary = "Search the catalog by title or author")
    public PageResponse<BookResponse> search(@RequestParam(required = false) String q, Pageable pageable) {
        return PageResponse.of(bookService.search(q, pageable).map(BookResponse::from));
    }
}
