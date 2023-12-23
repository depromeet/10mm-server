package com.depromeet.domain.missionrecord.dao;

import com.depromeet.domain.missionrecord.domain.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {}
