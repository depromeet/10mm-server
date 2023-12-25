package com.depromeet.domain.mission.service;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
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

    public Mission findMission(Long missionId) {
        return missionRepository
                .findByMissionId(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    public Slice<MissionResponse> listMission(Long memberId, int size, Long lastId) {
        Sort.Direction direction = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(0, size, Sort.by(direction, "id"));
        return missionRepository.findMissionList(memberId, pageable, lastId);
    }

    @Transactional
    public Mission modifyMission(ModifyMissionRequest modifyMissionRequest, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        mission.modifyMission(
                modifyMissionRequest.getName(),
                modifyMissionRequest.getContent(),
                modifyMissionRequest.getVisibility());
        return mission;
    }

    @Transactional
    public void removeMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }
}
