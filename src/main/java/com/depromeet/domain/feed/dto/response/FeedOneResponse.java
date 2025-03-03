package com.depromeet.domain.feed.dto.response;

import static com.depromeet.domain.reaction.dto.response.ReactionGroupByEmojiResponse.*;
import static java.util.Comparator.*;

import com.depromeet.domain.comment.dto.response.CommentDto;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.reaction.dto.response.ReactionGroupByEmojiResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record FeedOneResponse(
        @Schema(description = "작성자 ID", defaultValue = "1") Long memberId,
        @Schema(description = "작성자 닉네임", defaultValue = "default name") String nickname,
        @Schema(description = "작성자 프로필 이미지", defaultValue = "https://image.10mm.site/default.png")
                String profileImage,
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 일지 내용", defaultValue = "default remark") String remark,
        @Schema(
                        description = "미션 기록 인증 사진 Url",
                        defaultValue = "https://image.10mm.site/default.png")
                String recordImageUrl,
        @Schema(description = "미션 수행 시간 (분 단위)", defaultValue = "21") long duration,
        @Schema(description = "미션 시작한 지 N일차", defaultValue = "3") long sinceDay,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 시작 일시",
                        defaultValue = "2024-01-06 00:00:00",
                        type = "string")
                LocalDateTime startedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 종료 일시",
                        defaultValue = "2024-01-20 00:34:00",
                        type = "string")
                LocalDateTime finishedAt,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 일시",
                        defaultValue = "2024-01-06 00:00:00",
                        type = "string")
                LocalDateTime recordStartedAt,
        @Schema(description = "리액션 타입별 그룹") List<ReactionGroupByEmojiResponse> reactions,
        @Schema(description = "댓글 목록") List<CommentDto> comments) {
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
                calculateSinceDay(startedAt, recordStartedAt),
                startedAt,
                finishedAt,
                recordStartedAt,
                null,
                null);
    }

    // TODO: 다른 DTO에 존재하는 sinceDay 중복 계산 로직 제거
    private static long calculateSinceDay(LocalDateTime startedAt, LocalDateTime recordStartedAt) {
        return ChronoUnit.DAYS.between(startedAt, recordStartedAt) + 1;
    }

    public static FeedOneResponse from(MissionRecord missionRecord) {
        Mission mission = missionRecord.getMission();
        Member member = mission.getMember();

        List<ReactionGroupByEmojiResponse> reactions =
                groupByEmojiType(missionRecord.getReactions());

        List<CommentDto> comments =
                missionRecord.getComments().stream().map(CommentDto::from).toList();

        return new FeedOneResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                mission.getId(),
                mission.getName(),
                missionRecord.getId(),
                missionRecord.getRemark(),
                missionRecord.getImageUrl(),
                missionRecord.getDuration().toMinutes(),
                calculateSinceDay(mission.getStartedAt(), missionRecord.getStartedAt()),
                mission.getStartedAt(),
                mission.getFinishedAt(),
                missionRecord.getStartedAt(),
                reactions,
                comments);
    }
}
