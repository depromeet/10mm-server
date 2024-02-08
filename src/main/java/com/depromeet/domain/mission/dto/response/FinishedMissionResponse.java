package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record FinishedMissionResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 달성률", defaultValue = "1.1") double missionAttainRate,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 시작 시간",
                        defaultValue = "2024-01-01 00:34:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 종료 시간",
                        defaultValue = "2024-01-15 00:34:00",
                        type = "string")
                LocalDateTime finishedAt) {
    public static FinishedMissionResponse of(Mission mission, double missionAttainRate) {
        return new FinishedMissionResponse(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory(),
                missionAttainRate,
                mission.getStartedAt(),
                mission.getFinishedAt());
    }
}
