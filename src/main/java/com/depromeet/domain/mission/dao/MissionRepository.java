package com.depromeet.domain.mission.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depromeet.domain.mission.domain.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {}
