package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionVisibility;
import java.time.LocalDateTime;
import java.util.List;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);

    List<Mission> findInProgressMissionsWithRecords(Long memberId);

    List<Mission> findMissionsWithRecordsByRelations(Long memberId, boolean existsMemberRelations);

    List<Mission> findFeedAllByMemberId(Long memberId, List<MissionVisibility> visibilities);

    void updateFinishedDurationStatus(LocalDateTime today);
}
