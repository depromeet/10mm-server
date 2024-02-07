package com.depromeet.domain.follow.dto.response;

import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import java.util.List;

public record FollowListResponse(
        String targetNickname,
        List<MemberSearchResponse> followingList,
        List<MemberSearchResponse> followerList) {
    public static FollowListResponse of(
            String targetNickname,
            List<MemberSearchResponse> followingList,
            List<MemberSearchResponse> followerList) {
        return new FollowListResponse(targetNickname, followingList, followerList);
    }
}
