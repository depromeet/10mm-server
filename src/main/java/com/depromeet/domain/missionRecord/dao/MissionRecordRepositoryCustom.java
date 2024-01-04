package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.YearMonth;
import java.util.List;

public interface MissionRecordRepositoryCustom {

    List<MissionRecord> findAllByMissionId(Long missionId, YearMonth yearMonth);
}
