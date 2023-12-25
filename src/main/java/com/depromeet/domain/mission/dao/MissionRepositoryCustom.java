package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {
    Optional<Mission> findByMissionId(Long missionId);

    Slice<MissionResponse> findMissionList(Long memberId, Pageable pageable, Long lastId);
}
