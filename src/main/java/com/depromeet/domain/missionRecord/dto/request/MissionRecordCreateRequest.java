package com.depromeet.domain.missionRecord.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MissionRecordCreateRequest(
        @NotNull(message = "미션 아이디는 비워둘 수 없습니다.")
                @Schema(description = "미션 아이디", defaultValue = "1")
                Long missionId,
        @NotNull(message = "미션 기록 시작 시간은 비워둘 수 없습니다.")
                @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 시작 시간",
                        defaultValue = "2024-01-03 00:00:00",
                        type = "string")
                LocalDateTime startedAt,
        @NotNull(message = "미션 기록 종료 시간은 비워둘 수 없습니다.")
                @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "미션 기록 종료 시간",
                        defaultValue = "2023-01-03 00:34:00",
                        type = "string")
                LocalDateTime finishedAt,
        @NotNull(message = "미션 참여 한 시간(분)은 비워둘 수 없습니다.")
                @Schema(description = "미션 참여 한 시간(분)", defaultValue = "32")
                Integer durationMin,
        @NotNull(message = "미션 참여 한 시간(초)은 비워둘 수 없습니다.")
                @Schema(description = "미션 참여 한 시간(초)", defaultValue = "14")
                Integer durationSec) {}
