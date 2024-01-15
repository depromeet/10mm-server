package com.depromeet.domain.mission.application;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.*;
import com.depromeet.domain.missionRecord.dao.MissionRecordTtlRepository;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.domain.MissionRecordTtl;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionRecordTtlRepository missionRecordTtlRepository;
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
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        return MissionFindResponse.from(mission);
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정. 읽기 전용으로 설정한다.
    public List<MissionFindAllResponse> findAllMission() {
        Member currentMember = memberUtil.getCurrentMember();
        LocalDate localDate = LocalDate.now();

        List<Mission> missions = missionRepository.findMissionsWithRecords(currentMember.getId());

        List<MissionFindAllResponse> results = new ArrayList<>();
        for (Mission mission : missions) {
            List<MissionRecord> records = mission.getMissionRecords();

            Optional<MissionRecord> optionalRecord =
                    records.stream()
                            .filter(
                                    record -> {
                                        System.out.println(record.getStartedAt().toLocalDate());
                                        return record.getStartedAt()
                                                .toLocalDate()
                                                .equals(localDate);
                                    })
                            .findFirst();

            // 당일 수행한 미션기록이 없으면 NONE
            if (optionalRecord.isEmpty()) {
                results.add(MissionFindAllResponse.of(mission, MissionStatus.NONE, null));
                continue;
            }

            // 당일 수행한 미션기록의 인증사진이 존재하면 COMPLETE
            if (optionalRecord.get().getUploadStatus() == ImageUploadStatus.COMPLETE) {
                results.add(MissionFindAllResponse.of(mission, MissionStatus.COMPLETED, null));
                continue;
            }

            // 레디스에 미션기록의 인증사진 인증 대기시간 값이 존재하면 REQUIRED
            Optional<MissionRecordTtl> missionRecordTTL =
                    missionRecordTtlRepository.findById(
                            optionalRecord.get().getId());

            if (missionRecordTTL.isPresent()) {
                results.add(
                        MissionFindAllResponse.of(
                                mission,
                                MissionStatus.REQUIRED,
                                missionRecordTTL.get().getTtlFinishedAt()));
                continue;
            }

            throw new CustomException(ErrorCode.MISSION_STATUS_MISMATCH);
        }

        return results;
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
        return MissionUpdateResponse.from(mission);
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
