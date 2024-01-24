package com.depromeet.domain.follow.dto.response;

import com.depromeet.domain.member.domain.Member;

public record FollowedMemberResponse(Long memberId, String nickname, String profileImageUrl) {
    public static FollowedMemberResponse of(Member member) {
        return new FollowedMemberResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl());
    }
}
