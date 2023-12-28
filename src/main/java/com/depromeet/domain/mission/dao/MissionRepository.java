package com.depromeet.domain.mission.dao;

import java.util.Optional;

import com.depromeet.domain.mission.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
	Integer findMaxSortByMemberId(Long memberId);
}
