package com.depromeet.domain.member.dto.response;

import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.domain.member.domain.MemberStatus;
import com.depromeet.domain.member.domain.MemberVisibility;
import java.time.LocalDateTime;

public record MemberFindOneResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        ImageFileExtension imageFileExtension,
        MemberStatus memberStatus,
        MemberRole memberRole,
        MemberVisibility memberVisibility,
        String username,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static MemberFindOneResponse of(Member member, ImageFileExtension imageFileExtension) {
        return new MemberFindOneResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                imageFileExtension,
                member.getStatus(),
                member.getRole(),
                member.getVisibility(),
                member.getUsername(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}
