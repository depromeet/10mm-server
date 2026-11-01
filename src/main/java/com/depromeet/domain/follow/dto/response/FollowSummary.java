package com.depromeet.domain.follow.dto.response;

public record FollowSummary(long followingCount, long followerCount) {
    public static FollowSummary from(FollowCountsProjection projection) {
        return new FollowSummary(projection.getFollowingCount(), projection.getFollowerCount());
    }
}
