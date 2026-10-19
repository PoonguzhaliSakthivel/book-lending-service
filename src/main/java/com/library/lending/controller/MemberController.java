package com.library.lending.controller;

import com.library.lending.domain.Member;
import com.library.lending.dto.MemberRequest;
import com.library.lending.dto.MemberResponse;
import com.library.lending.dto.PageResponse;
import com.library.lending.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Library member management")
@PreAuthorize("hasRole('LIBRARIAN')")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Register a new member")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        Member member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member by id")
    public MemberResponse getById(@PathVariable Long id) {
        return MemberResponse.from(memberService.getMember(id));
    }

    @GetMapping
    @Operation(summary = "List members")
    public PageResponse<MemberResponse> list(Pageable pageable) {
        return PageResponse.of(memberService.list(pageable).map(MemberResponse::from));
    }
}
