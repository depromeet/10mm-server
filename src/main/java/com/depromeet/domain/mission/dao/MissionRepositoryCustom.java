package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);

    List<Mission> findInProgressMissionsWithRecords(Long memberId);

    List<Mission> findMissionsWithRecordsByRelations(Long memberId, boolean existsMemberRelations);

    void updateFinishedDurationStatus(LocalDateTime today);

    List<Mission> findAllFinishedMission(Long memberId);

    List<Mission> findMissionsWithRecordsByDate(LocalDate date, Long memberId);

    List<Mission> findAllInProgressMission();
}
