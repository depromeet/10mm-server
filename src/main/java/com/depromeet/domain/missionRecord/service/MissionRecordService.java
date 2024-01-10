package com.depromeet.domain.missionRecord.service;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.dao.MissionRecordTTLRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.domain.MissionRecordTTL;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindOneResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindResponse;
import com.depromeet.global.common.constants.RedisExpireEventConstants;
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
    private static final int EXPIRATION_TIME = 10;

    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRecordTTLRepository missionRecordTTLRepository;

    public Long createMissionRecord(MissionRecordCreateRequest request) {
        final Mission mission = findMissionById(request.missionId());
        final Member member = memberUtil.getCurrentMember();

        Duration duration =
                Duration.ofMinutes(request.durationMin()).plusSeconds(request.durationSec());

        validateMissionRecordUserMismatch(mission, member);
        validateMissionRecordDuration(duration);

        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        duration, request.startedAt(), request.finishedAt(), mission);
        Long expirationTime =
                Duration.between(
                                request.finishedAt(),
                                request.finishedAt().plusMinutes(EXPIRATION_TIME))
                        .getSeconds();
        MissionRecord createdMissionRecord = missionRecordRepository.save(missionRecord);
        missionRecordTTLRepository.save(
                MissionRecordTTL.createMissionRecordTTL(
                        RedisExpireEventConstants.EXPIRE_EVENT_IMAGE_UPLOAD_TIME_END.getValue()
                                + createdMissionRecord.getId(),
                        expirationTime));
        return createdMissionRecord.getId();
    }

    private Mission findMissionById(Long missionId) {
        return missionRepository
                .findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
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

    private void validateMissionRecordDuration(Duration duration) {
        if (duration.getSeconds() > 3600L) {
            throw new CustomException(ErrorCode.MISSION_RECORD_DURATION_OVERBALANCE);
        }
    }

    private void validateMissionRecordUserMismatch(Mission mission, Member member) {
        if (!mission.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.MISSION_RECORD_USER_MISMATCH);
        }
    }
}
