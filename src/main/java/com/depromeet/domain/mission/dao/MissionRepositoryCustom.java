package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import java.time.LocalDateTime;
import java.util.List;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);

    void updateFinishedDurationStatus(LocalDateTime today);
}
