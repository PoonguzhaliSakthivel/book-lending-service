package com.library.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.lending.config.SecurityConfig;
import com.library.lending.domain.Book;
import com.library.lending.dto.BookRequest;
import com.library.lending.exception.BookNotFoundException;
import com.library.lending.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private Book sampleBook() {
        return Book.builder().id(1L).title("Clean Code").author("Robert Martin").isbn("ISBN-1").totalCopies(3).availableCopies(3).build();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createBook_asLibrarian_returns201() throws Exception {
        when(bookService.createBook(any())).thenReturn(sampleBook());

        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BookRequest("Clean Code", "Robert Martin", "ISBN-1", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("ISBN-1"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createBook_asMember_isForbidden() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BookRequest("Clean Code", "Robert Martin", "ISBN-1", 3))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_withoutAuth_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BookRequest("Clean Code", "Robert Martin", "ISBN-1", 3))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createBook_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BookRequest("", "Robert Martin", "ISBN-1", 3))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getById_isAccessibleToAnyAuthenticatedRole() throws Exception {
        when(bookService.getBook(1L)).thenReturn(sampleBook());

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getById_notFound_returns404() throws Exception {
        when(bookService.getBook(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void search_returnsPagedResults() throws Exception {
        when(bookService.search(any(), any())).thenReturn(new PageImpl<>(List.of(sampleBook())));

        mockMvc.perform(get("/api/books").param("q", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].isbn").value("ISBN-1"));
    }
}
