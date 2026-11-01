package com.depromeet.domain.feed.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record FeedOneByProfileResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(
                        description = "미션 기록 인증 사진 Url",
                        defaultValue = "https://image.10mm.site/default.png")
                String recordImageUrl,
        @Schema(description = "미션 수행한 시간", defaultValue = "21") long duration,
        @Schema(description = "미션 시작한 지 N일차", defaultValue = "3") long sinceDay,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2023-01-06 00:00:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 종료 시간",
                        defaultValue = "2024-01-20 00:34:00",
                        type = "string")
                LocalDateTime finishedAt) {

    public FeedOneByProfileResponse(
            Long missionId,
            Long recordId,
            String name,
            String recordImageUrl,
            Duration duration,
            LocalDateTime missionStartedAt,
            LocalDateTime startedAt,
            LocalDateTime finishedAt) {
        this(
                missionId,
                recordId,
                name,
                recordImageUrl,
                duration.toMinutes(),
                ChronoUnit.DAYS.between(missionStartedAt.toLocalDate(), startedAt.toLocalDate())
                        + 1,
                startedAt,
                finishedAt);
    }
}
