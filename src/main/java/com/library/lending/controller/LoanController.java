package com.library.lending.controller;

import com.library.lending.domain.Loan;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.dto.LoanResponse;
import com.library.lending.dto.PageResponse;
import com.library.lending.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Borrowing and returning books")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book on behalf of a member")
    public LoanResponse borrow(@Valid @RequestBody BorrowRequest request) {
        Loan loan = loanService.borrow(request);
        return LoanResponse.from(loan);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return LoanResponse.from(loanService.returnLoan(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by id")
    public LoanResponse getById(@PathVariable Long id) {
        return LoanResponse.from(loanService.getLoan(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "List all loans")
    public PageResponse<LoanResponse> listAll(Pageable pageable) {
        return PageResponse.of(loanService.listAll(pageable).map(LoanResponse::from));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "List currently active (not yet returned) loans")
    public PageResponse<LoanResponse> listActive(Pageable pageable) {
        return PageResponse.of(loanService.listActive(pageable).map(LoanResponse::from));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "List overdue loans")
    public PageResponse<LoanResponse> listOverdue(Pageable pageable) {
        return PageResponse.of(loanService.listOverdue(pageable).map(LoanResponse::from));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "List loan history for a member")
    public PageResponse<LoanResponse> listForMember(@PathVariable Long memberId, Pageable pageable) {
        return PageResponse.of(loanService.listForMember(memberId, pageable).map(LoanResponse::from));
    }
}
