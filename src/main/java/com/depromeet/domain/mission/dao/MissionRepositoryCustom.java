package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {
    Optional<Mission> findByMissionId(Long missionId);

    Slice<Mission> findAllMission(Member member, Pageable pageable, Long lastId);
}
