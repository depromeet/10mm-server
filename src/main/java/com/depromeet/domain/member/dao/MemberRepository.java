package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    boolean existsByUsername(String username);
}
