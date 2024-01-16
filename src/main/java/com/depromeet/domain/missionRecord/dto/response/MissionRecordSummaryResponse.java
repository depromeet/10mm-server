package com.depromeet.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordSummaryResponse(
        @Schema(description = "번개 수", defaultValue = "1") long symbolStack,
        @Schema(description = "전체 누적 시간 (시간)", defaultValue = "1") long totalMissionHour,
        @Schema(description = "전체 누적 시간 (분)", defaultValue = "1") long totalMissionMinute,
        @Schema(description = "총 미션 달성률", defaultValue = "1.1") double totalMissionAttainRate) {
    public static MissionRecordSummaryResponse from(
            long stack,
            long totalMissionHour,
            long totalMissionMinute,
            double totalMissionAttainRate) {
        return new MissionRecordSummaryResponse(
                stack, totalMissionHour, totalMissionMinute, totalMissionAttainRate);
    }
}
