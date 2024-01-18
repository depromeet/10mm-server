package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
    Mission findTopByMemberOrderBySortDesc(Member member);

    @Modifying
    @Query(
            value =
                    "update Mission m set m.durationStatus='FINISHED' where m.finishedAt <= :today and m.durationStatus != 'FINISHED'")
    void updateFinishedDurationStatus(@Param("today") LocalDateTime today);
}
