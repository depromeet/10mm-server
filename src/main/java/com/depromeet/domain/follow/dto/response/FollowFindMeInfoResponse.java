package com.depromeet.domain.follow.dto.response;

public record FollowFindMeInfoResponse(Long followingCount, Long followerCount) {
    public static FollowFindMeInfoResponse of(Long followingCount, Long followerCount) {
        return new FollowFindMeInfoResponse(followingCount, followerCount);
    }
}
