package com.depromeet.domain.missionRecord.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.depromeet.domain.missionRecord.domain.MissionRecord;

@Repository
public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {}
