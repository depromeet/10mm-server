package com.depromeet.domain.mission.dao;

import com.depromeet.domain.mission.dto.MissionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {
    Slice<MissionResponse> findMissionList(Long memberId, Pageable pageable, Long lastId);
}
