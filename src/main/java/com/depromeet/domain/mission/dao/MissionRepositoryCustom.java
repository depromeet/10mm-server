package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);

    List<Mission> findInProgressMissionsWithRecords(Long memberId);

    List<Mission> findMissionsWithRecordsByRelations(Long memberId, boolean existsMemberRelations);

    void updateFinishedDurationStatus(LocalDateTime today);

    Slice<Mission> findAllFinishedMission(Long memberId, int size, Long lastId);
}
