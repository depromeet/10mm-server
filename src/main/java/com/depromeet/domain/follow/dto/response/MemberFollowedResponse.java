package com.depromeet.domain.follow.dto.response;

import com.depromeet.domain.member.domain.Member;

public record MemberFollowedResponse(Long memberId, String nickname, String profileImageUrl) {
    public static MemberFollowedResponse of(Member member) {
        return new MemberFollowedResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl());
    }
}
