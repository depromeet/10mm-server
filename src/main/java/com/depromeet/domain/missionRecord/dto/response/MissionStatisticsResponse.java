package com.depromeet.domain.missionRecord.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record MissionStatisticsResponse(
        @Schema(description = "수행 시간(시간)", defaultValue = "3") long totalMissionHour,
        @Schema(description = "수행 시간(분)", defaultValue = "8") long totalMissionMinute,
        @Schema(description = "번개 수", defaultValue = "8") long totalSymbolStack,
        @Schema(description = "연속 성공일", defaultValue = "4") long continuousSuccessDay,
        @Schema(description = "총 성공일", defaultValue = "9") long totalSuccessDay,
        @Schema(description = "달성률", defaultValue = "20.4") double totalMissionAttainRate,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "집중 시작 시각",
                        defaultValue = "2024-01-01 00:34:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "집중 마친 시각",
                        defaultValue = "2024-01-15 00:34:00",
                        type = "string")
                LocalDateTime finishedAt,
        @Schema(description = "미션 수행 타임 테이블") List<FocusMissionTimeOfDay> timeTable) {

    public static MissionStatisticsResponse of(
            long totalMissionHour,
            long totalMissionMinute,
            long totalSymbolStack,
            long continuousSuccessDay,
            long totalSuccessDay,
            double totalMissionAttainRate,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            List<FocusMissionTimeOfDay> timeTable) {
        return new MissionStatisticsResponse(
                totalMissionHour,
                totalMissionMinute,
                totalSymbolStack,
                continuousSuccessDay,
                totalSuccessDay,
                totalMissionAttainRate,
                startedAt,
                finishedAt,
                timeTable);
    }
}
