package com.depromeet.domain.mission.dao;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.mission.domain.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
    Mission findTopByMemberOrderBySortDesc(Member member);

    @EntityGraph(
            attributePaths = {"member"},
            type = FETCH)
    List<Mission> findAllByDurationStatus(DurationStatus status);
}
