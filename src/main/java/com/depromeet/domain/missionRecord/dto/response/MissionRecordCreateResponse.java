package com.depromeet.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordCreateResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long missionId) {

    public static MissionRecordCreateResponse from(Long missionId) {
        return new MissionRecordCreateResponse(missionId);
    }
}
