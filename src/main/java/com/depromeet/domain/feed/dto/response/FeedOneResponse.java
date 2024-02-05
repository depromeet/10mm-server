package com.depromeet.domain.feed.dto.response;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.LocalDateTime;

public record FeedOneResponse(
        @Schema(description = "작성자 ID", defaultValue = "1") Long targetId,
        @Schema(description = "작성자 닉네임", defaultValue = "default nickname") String nickname,
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
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
                LocalDateTime finishedAt) {
    public static FeedOneResponse of(Mission mission, MissionRecord missionRecord, Member member) {
        return new FeedOneResponse(
                mission.getMember().getId(),
                member.getProfile().getNickname(),
                mission.getId(),
                missionRecord.getId(),
                mission.getName(),
                missionRecord.getRemark(),
                missionRecord.getImageUrl(),
                missionRecord.getDuration().toMinutes(),
                Duration.between(missionRecord.getStartedAt(), LocalDateTime.now()).toDays() + 1,
                missionRecord.getStartedAt(),
                missionRecord.getFinishedAt());
    }
}
