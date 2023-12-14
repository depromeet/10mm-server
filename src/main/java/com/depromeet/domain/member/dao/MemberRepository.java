package com.depromeet.domain.member.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depromeet.domain.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {}
