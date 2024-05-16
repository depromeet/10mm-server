package com.depromeet.domain.mission.dao;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
    Mission findTopByMemberOrderBySortDesc(Member member);
}
