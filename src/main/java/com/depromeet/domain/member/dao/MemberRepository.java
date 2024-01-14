package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    boolean existsByUsername(String username);
}
