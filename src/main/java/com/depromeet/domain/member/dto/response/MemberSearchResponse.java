package com.depromeet.domain.member.dto.response;

import com.depromeet.domain.follow.dto.response.FollowStatus;
import com.depromeet.domain.member.domain.Member;

public record MemberSearchResponse(
        Long memberId, String nickname, String profileImageUrl, FollowStatus followStatus) {
    public static MemberSearchResponse toFollowingResponse(Member member) {
        return new MemberSearchResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                FollowStatus.FOLLOWING);
    }

    public static MemberSearchResponse toNotFollowingResponse(Member member) {
        return new MemberSearchResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                FollowStatus.NOT_FOLLOWING);
    }

    public static MemberSearchResponse toFollowedByMeResponse(Member member) {
        return new MemberSearchResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                FollowStatus.FOLLOWED_BY_ME);
    }
}
