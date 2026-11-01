package com.depromeet.domain.member.dto.response;

import com.depromeet.domain.follow.dto.response.FollowStatus;
import com.depromeet.domain.member.domain.Member;
import java.util.Set;

public record MemberSearchResponse(
        Long memberId, String nickname, String profileImageUrl, FollowStatus followStatus) {

    public static MemberSearchResponse of(
            Member member, Set<Long> followingIds, Set<Long> followerIds) {
        if (followingIds.contains(member.getId())) {
            return of(member, FollowStatus.FOLLOWING);
        }
        if (followerIds.contains(member.getId())) {
            return of(member, FollowStatus.FOLLOWED_BY_ME);
        }
        return of(member, FollowStatus.NOT_FOLLOWING);
    }

    private static MemberSearchResponse of(Member member, FollowStatus followStatus) {
        return new MemberSearchResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                followStatus);
    }
}
