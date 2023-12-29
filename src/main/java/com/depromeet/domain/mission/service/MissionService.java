package com.depromeet.domain.mission.service;

import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberUtil memberUtil;

    @Transactional
    public Mission addMission(CreateMissionRequest createMissionRequest) {
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

    public MissionResponse findMission(Long missionId) {
        return missionRepository
                .findByMissionId(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    public Slice<MissionResponse> listMission(int size, Long lastId) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

        return missionRepository.findMissionList(memberUtil.getCurrentMember(), pageable, lastId);
    }

    @Transactional
    public Mission modifyMission(ModifyMissionRequest modifyMissionRequest, Long missionId) {
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
    public void removeMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }
}
