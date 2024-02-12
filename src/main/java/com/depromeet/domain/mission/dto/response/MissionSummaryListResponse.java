package com.depromeet.domain.mission.dto.response;

import java.util.List;

public record MissionSummaryListResponse(
        long missionAllCount,
        long missionCompleteCount,
        long missionNoneCount,
        List<MissionSummaryItem> missionSummaryItems) {
    public static MissionSummaryListResponse of(
            long missionAllCount,
            long missionCompleteCount,
            long missionNoneCount,
            List<MissionSummaryItem> missionSummaryItems) {
        return new MissionSummaryListResponse(
                missionAllCount, missionCompleteCount, missionNoneCount, missionSummaryItems);
    }
}
