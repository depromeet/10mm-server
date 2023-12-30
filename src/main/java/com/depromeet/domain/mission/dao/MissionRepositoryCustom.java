package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {
    Optional<MissionResponse> findByMissionId(Long missionId);

    Slice<MissionResponse> findAllMission(Member member, Pageable pageable, Long lastId);
}
