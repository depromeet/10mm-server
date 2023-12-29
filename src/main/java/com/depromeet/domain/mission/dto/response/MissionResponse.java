package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionResponse(
        @Schema(description = "미션 ID") Long missionId,
        @Schema(description = "미션 이름") String name,
        @Schema(description = "미션 내용") String content,
        @Schema(description = "미션 카테고리") String category,
        @Schema(description = "미션 공개여부") String visibility,
        @Schema(description = "미션 정렬 값") Integer sort) {
    public MissionResponse(Mission mission) {
        this(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory().getValue(),
                mission.getVisibility().getValue(),
                mission.getSort());
    }
}
