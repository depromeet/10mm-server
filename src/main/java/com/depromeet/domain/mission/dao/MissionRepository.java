package com.depromeet.domain.mission.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.depromeet.domain.mission.domain.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {}
