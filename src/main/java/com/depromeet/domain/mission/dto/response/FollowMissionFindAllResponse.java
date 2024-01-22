package com.depromeet.domain.mission.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FollowMissionFindAllResponse(
        @Schema(description = "번개 수", defaultValue = "1") long symbolStack,
        @Schema(description = "친구 미션 목록") List<MissionFindAllResponse> followMissions) {
    public static FollowMissionFindAllResponse from(
            long symbolStack, List<MissionFindAllResponse> followMissions) {
        return new FollowMissionFindAllResponse(symbolStack, followMissions);
    }
}
