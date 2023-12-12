package com.depromeet.domain.member.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.depromeet.domain.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {}
