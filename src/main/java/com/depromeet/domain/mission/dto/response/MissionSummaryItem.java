package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MissionSummaryItem(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 공개여부", defaultValue = "ALL") MissionVisibility visibility,
        @Schema(description = "미션 상태", defaultValue = "1") MissionStatus missionStatus,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 종료 시간",
                        defaultValue = "2024-01-03 00:34:00",
                        type = "string")
                LocalDateTime finishedAt) {

    public static MissionSummaryItem of(Mission mission, MissionStatus missionStatus) {
        return new MissionSummaryItem(
                mission.getId(),
                mission.getName(),
                mission.getCategory(),
                mission.getVisibility(),
                missionStatus,
                mission.getFinishedAt());
    }
}
