package com.depromeet.domain.mission.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRepositoryCustom {

    Slice<Mission> findAllMission(Member member, Pageable pageable, Long lastId);
}
