package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
    Mission findTopByMemberOrderBySortDesc(Member member);

	@Modifying
	@Query(value = "update Mission m set m.durationStatus='FINISHED' where m.finishedAt <= NOW()")
	void updateMissionDurationStatusComplete();
}
