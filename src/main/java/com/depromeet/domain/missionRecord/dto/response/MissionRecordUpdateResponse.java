package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordUpdateResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 기록 일지", defaultValue = "default missionRecord remark")
                String remark) {
    public MissionRecordUpdateResponse(MissionRecord missionRecord) {
        this(missionRecord.getId(), missionRecord.getRemark());
    }
}
