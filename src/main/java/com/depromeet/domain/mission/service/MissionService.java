package com.depromeet.domain.mission.service;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.CreateMissionRequest;
import com.depromeet.domain.mission.dto.MissionResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MemberRepository memberRepository;
    private final MissionRepository missionRepository;

    @Transactional
    public Mission addMission(CreateMissionRequest createMissionRequest, Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Mission mission =
                Mission.createMission(
                        createMissionRequest.getName(),
                        createMissionRequest.getContent(),
                        1,
                        createMissionRequest.getCategory(),
                        createMissionRequest.getVisibility(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusWeeks(2),
                        member);
        return missionRepository.save(mission);
    }

    public Slice<MissionResponse> listMission(Long memberId, int size, Long lastId) {
        Sort.Direction direction = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(0, size, Sort.by(direction, "id"));
        return missionRepository.findMissionList(memberId, pageable, lastId);
    }
}
