package com.depromeet.domain.member.dao;

import com.depromeet.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findNonCompleteMissions(LocalDateTime today);
}
