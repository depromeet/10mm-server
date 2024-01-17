package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import java.util.List;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {

    List<Mission> findMissionsWithRecords(Long memberId);

    Slice<Mission> findAllFinishedMission(Long memberId, int size, Long lastId);
}
