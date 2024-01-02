package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.ArchiveStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionFindResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 공개여부", defaultValue = "ALL") MissionVisibility visibility,
        @Schema(description = "미션 아카이빙 상태", defaultValue = "NONE") ArchiveStatus status,
        @Schema(description = "미션 정렬 값", defaultValue = "1") Integer sort) {
    public MissionFindResponse(Mission mission) {
        this(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory(),
                mission.getVisibility(),
                mission.getArchiveStatus(),
                mission.getSort());
    }

    public static MissionFindResponse from(Mission mission) {
        return new MissionFindResponse(mission);
    }
}
