package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionUpdateResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 공개여부", defaultValue = "ALL") MissionVisibility visibility) {
    public MissionUpdateResponse(Mission mission) {
        this(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory(),
                mission.getVisibility());
    }
}
