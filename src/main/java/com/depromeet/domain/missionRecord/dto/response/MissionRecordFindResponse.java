package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MissionRecordFindResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 기록 일지", defaultValue = "default MissionRecord Remark")
                String remark,
        @Schema(
                        description = "미션 기록 인증 사진 Url",
                        defaultValue = "https://image.10mm.site/default.png")
                String imageUrl,
        @Schema(description = "미션 시작 일자", defaultValue = "3") int missionDay,
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
    public static MissionRecordFindResponse from(MissionRecord missionRecord) {
        return new MissionRecordFindResponse(
                missionRecord.getId(),
                missionRecord.getRemark(),
                missionRecord.getImageUrl(),
                missionRecord.getStartedAt().getDayOfMonth(),
                missionRecord.getStartedAt(),
                missionRecord.getFinishedAt());
    }
}
