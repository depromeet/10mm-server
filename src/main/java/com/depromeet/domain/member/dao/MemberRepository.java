package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import io.lettuce.core.dynamic.annotation.Param;
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
		"SELECT m FROM Member m WHERE m.profile.nickname LIKE CONCAT('%', :searchNickname, '%') escape '_' AND m.profile.nickname != :myNickname")
    List<Member> nicknameSearch(
            @Param("searchNickname") String searchNickname, @Param("myNickname") String myNickname);
}
