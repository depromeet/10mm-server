package com.depromeet.domain.mission.dto.request;

import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MissionUpdateRequest(
        @NotBlank(message = "이름은 비워둘 수 없습니다.")
                @Size(min = 1, max = 20, message = "미션명은 1자 이상 20자 이하")
                @Schema(description = "미션 이름")
                String name,
        @Size(min = 1, max = 30, message = "미션 내용은 1자 이상 30자 이하") @Schema(description = "미션 내용")
                String content,
        @Schema(description = "미션 공개여부") MissionVisibility visibility) {}
