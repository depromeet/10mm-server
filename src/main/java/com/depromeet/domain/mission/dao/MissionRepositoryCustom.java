package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {
    Optional<MissionFindResponse> findByMissionId(Long missionId);

    Slice<MissionFindResponse> findAllMission(Member member, Pageable pageable, Long lastId);
}
