package com.depromeet.domain.follow.dto.response;

public interface FollowCountsProjection {
    Long getFollowingCount();

    Long getFollowerCount();
}
