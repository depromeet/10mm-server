package com.depromeet.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowerDeletedResponse(
        @Schema(description = "팔로워 삭제 후 나와 target과의 팔로우 상태", defaultValue = "NOT_FOLLOWING")
                FollowStatus followStatus) {
    public static FollowerDeletedResponse from(FollowStatus followStatus) {
        return new FollowerDeletedResponse(followStatus);
    }
    ;
}
