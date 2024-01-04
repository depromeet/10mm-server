package com.depromeet.domain.missionRecord.dao;

import java.util.List;

import com.depromeet.domain.missionRecord.domain.MissionRecord;

public interface MissionRecordRepositoryCustom {

	List<MissionRecord> findAllByMissionId(Long missionId, String year, String month);
}
