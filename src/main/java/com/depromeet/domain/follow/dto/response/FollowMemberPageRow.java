package com.depromeet.domain.follow.dto.response;

import com.depromeet.domain.member.domain.Member;
import java.time.LocalDateTime;

public record FollowMemberPageRow(Long relationId, LocalDateTime createdAt, Member member) {}
