package com.library.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.lending.config.SecurityConfig;
import com.library.lending.domain.Loan;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.exception.MaxActiveLoansExceededException;
import com.library.lending.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private LoanService loanService;

    @Test
    @WithMockUser(roles = "MEMBER")
    void borrow_succeeds_returns200WithDueDate() throws Exception {
        Loan loan = Loan.builder().id(1L).bookId(1L).memberId(2L)
                .borrowedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .dueDate(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();
        when(loanService.borrow(new BorrowRequest(1L, 2L))).thenReturn(loan);

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BorrowRequest(1L, 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-01-15T10:00:00"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void borrow_overLimit_returns409() throws Exception {
        when(loanService.borrow(new BorrowRequest(1L, 2L))).thenThrow(new MaxActiveLoansExceededException(2L, 3));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BorrowRequest(1L, 2L))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void listAll_asMember_isForbidden() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void listAll_asLibrarian_isOk() throws Exception {
        when(loanService.listAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk());
    }
}
