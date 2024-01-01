package com.depromeet.domain.mission.service;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
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
    public MissionCreateResponse createMission(MissionCreateRequest missionCreateRequest) {
        LocalDateTime startedAt = LocalDateTime.now();
        final Member member = memberUtil.getCurrentMember();

        Mission missionByMaxSort =
                missionRepository.findTopByMemberOrderBySortDesc(memberUtil.getCurrentMember());
        Integer maxSort = missionByMaxSort == null ? 1 : missionByMaxSort.getSort() + 1;

        Mission mission =
                Mission.createMission(
                        missionCreateRequest.name(),
                        missionCreateRequest.content(),
                        maxSort,
                        missionCreateRequest.category(),
                        missionCreateRequest.visibility(),
                        startedAt,
                        startedAt.plusWeeks(2),
                        member);
        return new MissionCreateResponse(missionRepository.save(mission));
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
    public MissionUpdateResponse updateMission(
            MissionUpdateRequest missionUpdateRequest, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        mission.modifyMission(
                missionUpdateRequest.name(),
                missionUpdateRequest.content(),
                missionUpdateRequest.visibility());
        return new MissionUpdateResponse(mission);
    }

    @Transactional
    public void deleteMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }
}
