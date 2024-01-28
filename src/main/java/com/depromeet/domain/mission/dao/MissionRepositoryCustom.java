package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import java.util.List;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);
}
