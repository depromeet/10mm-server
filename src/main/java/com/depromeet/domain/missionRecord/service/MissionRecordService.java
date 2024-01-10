package com.depromeet.domain.missionRecord.service;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindOneResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.Duration;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionRecordService {
    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;

    public Long createMissionRecord(MissionRecordCreateRequest request) {
        final Mission mission = findMission(request);
        final Member member = memberUtil.getCurrentMember();

        Duration duration =
                Duration.ofMinutes(request.durationMin()).plusSeconds(request.durationSec());

        validateMissionRecordUserMismatch(mission, member);
        validateMissionRecordDuration(duration);

        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        duration, request.startedAt(), request.finishedAt(), mission);
        return missionRecordRepository.save(missionRecord).getId();
    }

    @Transactional(readOnly = true)
    public MissionRecordFindOneResponse findOneMissionRecord(Long recordId) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
        return MissionRecordFindOneResponse.from(missionRecord);
    }

    @Transactional(readOnly = true)
    public List<MissionRecordFindResponse> findAllMissionRecord(
            Long missionId, YearMonth yearMonth) {
        List<MissionRecord> missionRecords =
                missionRecordRepository.findAllByMissionIdAndYearMonth(missionId, yearMonth);
        return missionRecords.stream().map(MissionRecordFindResponse::from).toList();
    }

    private Mission findMission(MissionRecordCreateRequest request) {
        return missionRepository
                .findById(request.missionId())
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    private void validateMissionRecordUserMismatch(Mission mission, Member member) {
        if (!member.getId().equals(mission.getMember().getId())) {
            throw new CustomException(ErrorCode.MISSION_RECORD_USER_MISMATCH);
        }
    }

    private void validateMissionRecordDuration(Duration duration) {
        if (duration.getSeconds() > 3600L) {
            throw new CustomException(ErrorCode.MISSION_RECORD_DURATION_OVERBALANCE);
        }
    }
}
