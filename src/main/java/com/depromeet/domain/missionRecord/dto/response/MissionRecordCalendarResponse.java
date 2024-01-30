package com.depromeet.domain.missionRecord.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record MissionRecordCalendarResponse(
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 시작 시간",
                        defaultValue = "2024-01-01 00:34:00",
                        type = "string")
                LocalDateTime missionStartedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 종료 시간",
                        defaultValue = "2024-01-15 00:34:00",
                        type = "string")
                LocalDateTime missionFinishedAt,
        @Schema(description = "미션 기록들") List<MissionRecordFindResponse> missionRecords) {
    public static MissionRecordCalendarResponse of(
            LocalDateTime missionStartedAt,
            LocalDateTime missionFinishedAt,
            List<MissionRecordFindResponse> missionRecords) {
        return new MissionRecordCalendarResponse(
                missionStartedAt, missionFinishedAt, missionRecords);
    }
}
