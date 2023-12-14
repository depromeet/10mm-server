package com.depromeet.domain.missionRecord.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depromeet.domain.missionRecord.domain.MissionRecord;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {}
