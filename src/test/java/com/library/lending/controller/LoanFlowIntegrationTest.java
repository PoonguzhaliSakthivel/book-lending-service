package com.library.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.lending.dto.BookRequest;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.dto.MemberRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void fullBorrowAndReturnFlow_respectsAvailabilityAndLoanLimit() throws Exception {
        String bookJson = mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BookRequest("Domain-Driven Design", "Eric Evans", "ISBN-DDD-1", 1))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long bookId = objectMapper.readTree(bookJson).get("id").asLong();

        String memberJson = mockMvc.perform(post("/api/members")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new MemberRequest("Ada Lovelace", "ada@example.com"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long memberId = objectMapper.readTree(memberJson).get("id").asLong();

        String loanJson = mockMvc.perform(post("/api/loans/borrow")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId, memberId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long loanId = objectMapper.readTree(loanJson).get("id").asLong();

        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(jsonPath("$.availableCopies").value(0));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId, memberId))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/loans/" + loanId + "/return"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(jsonPath("$.availableCopies").value(1));
    }
}
