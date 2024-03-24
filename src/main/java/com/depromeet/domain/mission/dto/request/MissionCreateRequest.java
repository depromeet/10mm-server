package com.depromeet.domain.mission.dto.request;

import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record MissionCreateRequest(
        @NotBlank(message = "이름은 비워둘 수 없습니다.")
                @Size(min = 1, max = 20, message = "미션명은 1자 이상 20자 이하")
                @Schema(description = "미션 이름", defaultValue = "default name")
                String name,
        @Size(max = 30, message = "미션 내용은 30자 이하")
                @Schema(description = "미션 내용", defaultValue = "default content")
                String content,
        @NotNull @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @NotNull @Schema(description = "미션 공개여부", defaultValue = "ALL")
                MissionVisibility visibility,
        @Schema(description = "미션 리마인드 알림 시간", defaultValue = "00:50:00", type = "string")
                LocalTime remindAt) {}
