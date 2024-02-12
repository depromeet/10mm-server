package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.YearMonth;
import java.util.List;

public interface MissionRecordRepositoryCustom {

    List<MissionRecord> findAllByMissionIdAndYearMonth(Long missionId, YearMonth yearMonth);

    List<MissionRecord> findAllByCompletedMission(Long missionId);

    List<FeedOneResponse> findFeedAll(List<Member> members);

    List<MissionRecord> findFeedAllByMemberId(Long memberId, List<MissionVisibility> visibilities);

    boolean isCompletedMissionExistsToday(Long missionId);

    void deleteByMissionRecordId(Long missionRecordId);
}
