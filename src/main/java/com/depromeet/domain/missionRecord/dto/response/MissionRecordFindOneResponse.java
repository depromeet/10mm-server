package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MissionRecordFindOneResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 기록 일지", defaultValue = "default MissionRecord Remark")
                String remark,
        @Schema(
                        description = "미션 기록 인증 사진 Url",
                        defaultValue = "https://image.10mm.today/default.png")
                String imageUrl,
        @Schema(description = "미션 수행한 시간", defaultValue = "21") long duration,
        @Schema(description = "미션 시작한 지 N일차", defaultValue = "3") long sinceDay,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2023-01-03 00:00:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 종료 시간",
                        defaultValue = "2024-01-03 00:34:00",
                        type = "string")
                LocalDateTime finishedAt) {
    public static MissionRecordFindOneResponse of(MissionRecord missionRecord, long sinceDay) {
        return new MissionRecordFindOneResponse(
                missionRecord.getId(),
                missionRecord.getRemark(),
                missionRecord.getImageUrl(),
                missionRecord.getDuration().toMinutes(),
                sinceDay,
                missionRecord.getStartedAt(),
                missionRecord.getFinishedAt());
    }
}
