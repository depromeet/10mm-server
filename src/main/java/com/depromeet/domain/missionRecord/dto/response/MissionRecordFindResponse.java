package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.LocalDateTime;

public record MissionRecordFindResponse(
        Long recordId,
        String remark,
        String imageUrl,
        int missionDay,
        LocalDateTime startedAt,
        LocalDateTime finishedAt) {
    public static MissionRecordFindResponse from(MissionRecord missionRecord) {
        return new MissionRecordFindResponse(
                missionRecord.getId(),
                missionRecord.getRemark(),
                missionRecord.getImageUrl(),
                missionRecord.getStartedAt().getDayOfMonth(),
                missionRecord.getStartedAt(),
                missionRecord.getFinishedAt());
    }
}
