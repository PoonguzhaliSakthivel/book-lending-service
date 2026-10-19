package com.library.lending.repository;

import com.library.lending.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
}
