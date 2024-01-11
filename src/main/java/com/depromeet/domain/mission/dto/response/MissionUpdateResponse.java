package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionUpdateResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId) {
    public static MissionUpdateResponse from(Mission mission) {
        return new MissionUpdateResponse(mission.getId());
    }
}
