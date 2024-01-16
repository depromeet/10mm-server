package com.depromeet.domain.member.dto.response;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.domain.member.domain.MemberStatus;
import com.depromeet.domain.member.domain.MemberVisibility;

import java.time.LocalDateTime;

public record MemberFindOneResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        MemberStatus memberStatus,
        MemberRole memberRole,
        MemberVisibility memberVisibility,
        String username,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MemberFindOneResponse from(Member member) {
        return new MemberFindOneResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                member.getStatus(),
                member.getRole(),
                member.getVisibility(),
                member.getUsername(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
