package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionFindResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "공부") String category,
        @Schema(description = "미션 공개여부", defaultValue = "공개") String visibility,
        @Schema(description = "미션 정렬 값", defaultValue = "1") Integer sort) {
    public MissionFindResponse(Mission mission) {
        this(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory().getValue(),
                mission.getVisibility().getValue(),
                mission.getSort());
    }
}
