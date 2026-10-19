package com.library.lending.service;

import com.library.lending.domain.Member;
import com.library.lending.dto.MemberRequest;
import com.library.lending.exception.DuplicateEmailException;
import com.library.lending.exception.MemberNotFoundException;
import com.library.lending.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            log.warn("Rejected createMember: email already registered");
            throw new DuplicateEmailException(request.email());
        }
        Member saved = memberRepository.save(Member.builder().name(request.name()).email(request.email()).build());
        log.info("Created member id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Member getMember(Long id) {
        log.debug("Fetching member id={}", id);
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Member> list(Pageable pageable) {
        log.debug("Listing members page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return memberRepository.findAll(pageable);
    }
}
