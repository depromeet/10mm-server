package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record FocusMissionRecordItem(
        @Schema(description = "번개 수", defaultValue = "3") long symbolStack,
        @Schema(description = "미션 수행 시간 (Minute)", defaultValue = "34") long durationMinute,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2024-01-01 00:34:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 종료 시간",
                        defaultValue = "2024-01-15 00:34:00",
                        type = "string")
                LocalDateTime finishedAt) {

    public static FocusMissionRecordItem from(MissionRecord record) {
        return new FocusMissionRecordItem(
                record.getDuration().toMinutes() / 10,
                record.getDuration().toMinutes(),
                record.getStartedAt(),
                record.getFinishedAt());
    }
}
