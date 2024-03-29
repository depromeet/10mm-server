package com.depromeet.domain.feed.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record FeedOneResponse(
        @Schema(description = "작성자 ID", defaultValue = "1") Long memberId,
        @Schema(description = "작성자 닉네임", defaultValue = "default name") String nickname,
        @Schema(description = "작성자 프로필 이미지", defaultValue = "https://image.10mm.today/default.png")
                String profileImage,
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 일지 내용", defaultValue = "default remark") String remark,
        @Schema(
                        description = "미션 기록 인증 사진 Url",
                        defaultValue = "https://image.10mm.today/default.png")
                String recordImageUrl,
        @Schema(description = "미션 수행한 시간", defaultValue = "21") long duration,
        @Schema(description = "미션 시작한 지 N일차", defaultValue = "3") long sinceDay,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2024-01-06 00:00:00",
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
                LocalDateTime finishedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2024-01-06 00:00:00",
                        type = "string")
                LocalDateTime recordStartedAt) {
    @QueryProjection
    public FeedOneResponse(
            Long memberId,
            String nickname,
            String profileImage,
            Long missionId,
            String name,
            Long recordId,
            String remark,
            String recordImageUrl,
            Duration duration,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime recordStartedAt) {
        this(
                memberId,
                nickname,
                profileImage,
                missionId,
                name,
                recordId,
                remark,
                recordImageUrl,
                duration.toMinutes(),
                ChronoUnit.DAYS.between(startedAt, recordStartedAt) + 1,
                startedAt,
                finishedAt,
                recordStartedAt);
    }

    public static FeedOneResponse of(
            Long memberId,
            String nickname,
            String profileImage,
            Long missionId,
            String name,
            Long recordId,
            String remark,
            String recordImageUrl,
            Duration duration,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime recordStartedAt) {
        return new FeedOneResponse(
                memberId,
                nickname,
                profileImage,
                missionId,
                name,
                recordId,
                remark,
                recordImageUrl,
                duration.toMinutes(),
                Duration.between(startedAt, LocalDateTime.now()).toDays() + 1,
                startedAt,
                finishedAt,
                recordStartedAt);
    }
}
