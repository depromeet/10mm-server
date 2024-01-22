package com.depromeet.domain.follow.dto.response;

public record FollowFindTargetInfoResponse(
        Long followingCount, Long followerCount, FollowStatus followStatus) {
    public static FollowFindTargetInfoResponse of(
            Long followingCount, Long followerCount, FollowStatus followStatus) {
        return new FollowFindTargetInfoResponse(followingCount, followerCount, followStatus);
    }
}
