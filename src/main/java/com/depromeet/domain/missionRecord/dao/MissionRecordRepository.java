package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {}
