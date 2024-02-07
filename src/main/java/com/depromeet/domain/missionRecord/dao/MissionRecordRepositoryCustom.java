package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.YearMonth;
import java.util.List;

public interface MissionRecordRepositoryCustom {

    List<MissionRecord> findAllByMissionIdAndYearMonth(Long missionId, YearMonth yearMonth);

    List<FeedOneResponse> findFeedAll(List<Long> sourceIds);

    List<MissionRecord> findFeedAllByMemberId(Long memberId, List<MissionVisibility> visibilities);

    boolean isCompletedMissionExistsToday(Long missionId);

    void deleteByMissionRecordId(Long missionRecordId);
}
