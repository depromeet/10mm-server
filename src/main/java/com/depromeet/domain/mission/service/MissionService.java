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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberUtil memberUtil;

    public MissionCreateResponse createMission(MissionCreateRequest missionCreateRequest) {
        Mission mission = createMissionEntity(missionCreateRequest);
		Mission saveMission = missionRepository.save(mission);
		return MissionCreateResponse.from(saveMission);
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정. 읽기 전용으로 설정한다.
    public MissionFindResponse findOneMission(Long missionId) {
        Mission mission =
                missionRepository
                        .findByMissionId(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        return MissionFindResponse.from(mission);
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정. 읽기 전용으로 설정한다.
    public Slice<MissionFindResponse> findAllMission(int size, Long lastId) {
        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
        Slice<Mission> mappedMissions =
                missionRepository.findAllMission(
                        memberUtil.getCurrentMember(), pageRequest, lastId);
        return mappedMissions.map(MissionFindResponse::new);
    }

    public MissionUpdateResponse updateMission(
            MissionUpdateRequest missionUpdateRequest, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        mission.updateMission(
                missionUpdateRequest.name(),
                missionUpdateRequest.content(),
                missionUpdateRequest.visibility());
        return new MissionUpdateResponse(mission);
    }

    public void deleteMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }

    private Integer maxSort(Member member) {
        Mission missionByMaxSort = missionRepository.findTopByMemberOrderBySortDesc(member);
        return missionByMaxSort == null ? 1 : missionByMaxSort.getSort() + 1;
    }

    private Mission createMissionEntity(MissionCreateRequest missionCreateRequest) {
        LocalDateTime startedAt = LocalDateTime.now();
        final Member member = memberUtil.getCurrentMember();
        Integer maxSort = maxSort(member);

        return Mission.createMission(
                missionCreateRequest.name(),
                missionCreateRequest.content(),
                maxSort,
                missionCreateRequest.category(),
                missionCreateRequest.visibility(),
                startedAt,
                startedAt.plusWeeks(2),
                member);
    }
}
