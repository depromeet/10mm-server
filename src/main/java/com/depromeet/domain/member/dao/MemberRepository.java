package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    boolean existsByUsername(String username);

    boolean existsByProfileNickname(String nickname);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByProfileNickname(String nickname);

    @Query(
            "SELECT m FROM Member m WHERE m.profile.nickname like %:searchNickname% AND m.profile.nickname != :myNickname")
    List<Member> nicknameSearch(String searchNickname, String myNickname);
}
