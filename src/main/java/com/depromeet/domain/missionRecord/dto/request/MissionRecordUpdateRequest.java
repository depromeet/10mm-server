package com.depromeet.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record MissionRecordUpdateRequest(
        @Size(min = 0, max = 200, message = "미션 기록 일지는 200자까지")
                @Schema(description = "미션 기록 일지", defaultValue = "default missionRecord remark")
                String remark) {}
