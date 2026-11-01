package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMissionNonCompletedMembers(LocalDateTime today);

    List<Member> nicknameSearchPage(
            String nickname,
            Long currentMemberId,
            Integer cursorPriority,
            String cursorNickname,
            Long cursorMemberId,
            int limit);
}
