package com.library.lending.service;

import com.library.lending.domain.Member;
import com.library.lending.dto.MemberRequest;
import com.library.lending.exception.DuplicateEmailException;
import com.library.lending.exception.MemberNotFoundException;
import com.library.lending.repository.MemberRepository;
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
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void createMember_succeeds() {
        when(memberRepository.existsByEmail("a@x.com")).thenReturn(false);
        when(memberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Member member = memberService.createMember(new MemberRequest("Alice", "a@x.com"));

        assertThat(member.getName()).isEqualTo("Alice");
    }

    @Test
    void createMember_rejectsDuplicateEmail() {
        when(memberRepository.existsByEmail("a@x.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(new MemberRequest("Alice", "a@x.com")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getMember_throwsWhenMissing() {
        when(memberRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(9L))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
