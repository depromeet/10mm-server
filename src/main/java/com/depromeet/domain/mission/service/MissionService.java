package com.depromeet.domain.mission.service;

import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberUtil memberUtil;

    @Transactional
    public Mission craeteMission(CreateMissionRequest createMissionRequest) {
        LocalDateTime startedAt = LocalDateTime.now();

        Mission missionByMaxSort =
                missionRepository.findTopByMemberOrderBySortDesc(memberUtil.getCurrentMember());
        Integer maxSort = missionByMaxSort == null ? 1 : missionByMaxSort.getSort() + 1;
        Mission mission =
                Mission.createMission(
                        createMissionRequest.name(),
                        createMissionRequest.content(),
                        maxSort,
                        createMissionRequest.category(),
                        createMissionRequest.visibility(),
                        startedAt,
                        startedAt.plusWeeks(2),
                        memberUtil.getCurrentMember());
        return missionRepository.save(mission);
    }

    public MissionFindResponse findOneMission(Long missionId) {
        return missionRepository
                .findByMissionId(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    public Slice<MissionFindResponse> findAllMission(Pageable pageable, Long lastId) {
        PageRequest pageRequest = PageRequest.of(0, pageable.getPageSize(), pageable.getSort());
        return missionRepository.findAllMission(memberUtil.getCurrentMember(), pageRequest, lastId);
    }

    @Transactional
    public Mission updateMission(ModifyMissionRequest modifyMissionRequest, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        mission.modifyMission(
                modifyMissionRequest.name(),
                modifyMissionRequest.content(),
                modifyMissionRequest.visibility());
        return mission;
    }

    @Transactional
    public void deleteMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }
}
