package com.depromeet.domain.missionRecord.application;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.dao.MissionRecordTtlRepository;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.domain.MissionRecordTtl;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordUpdateRequest;
import com.depromeet.domain.missionRecord.dto.response.*;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionRecordService {
    private static final int EXPIRATION_TIME = 10;
    private static final int DAYS_ADJUSTMENT = 1;

    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRecordTtlRepository missionRecordTtlRepository;

    public MissionRecordCreateResponse createMissionRecord(MissionRecordCreateRequest request) {
        final Mission mission = findMissionById(request.missionId());
        final Member member = memberUtil.getCurrentMember();

        Duration duration =
                Duration.ofMinutes(request.durationMin()).plusSeconds(request.durationSec());

        validateMissionRecordUserMismatch(mission, member);
        validateMissionRecordDuration(duration);
        validateMissionRecordExistsToday(mission.getId());

        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        duration, request.startedAt(), request.finishedAt(), mission);
        Long expirationTime =
                Duration.between(
                                request.finishedAt(),
                                request.finishedAt().plusMinutes(EXPIRATION_TIME))
                        .getSeconds();
        MissionRecord createdMissionRecord = missionRecordRepository.save(missionRecord);
        missionRecordTtlRepository.save(
                MissionRecordTtl.createMissionRecordTtl(
                        createdMissionRecord.getId(),
                        expirationTime,
                        request.finishedAt().plusMinutes(EXPIRATION_TIME)));
        return MissionRecordCreateResponse.from(createdMissionRecord.getId());
    }

    private void validateMissionRecordExistsToday(Long missionId) {
        if (missionRecordRepository.isCompletedMissionExistsToday(missionId)) {
            throw new CustomException(ErrorCode.MISSION_RECORD_ALREADY_EXISTS_TODAY);
        }
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
        long sinceDay =
                Duration.between(missionRecord.getMission().getStartedAt(), LocalDateTime.now())
                                .toDays()
                        + DAYS_ADJUSTMENT;
        return MissionRecordFindOneResponse.of(missionRecord, sinceDay);
    }

    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse findAllMissionRecord(Long missionId, YearMonth yearMonth) {
        List<MissionRecord> missionRecords =
                missionRecordRepository.findAllByMissionIdAndYearMonth(missionId, yearMonth);
        List<MissionRecordFindResponse> missionRecordFindResponses =
                missionRecords.stream().map(MissionRecordFindResponse::from).toList();
        Mission mission = findMissionById(missionId);
        return MissionRecordCalendarResponse.of(
                mission.getStartedAt(), mission.getFinishedAt(), missionRecordFindResponses);
    }

    public MissionRecordUpdateResponse updateMissionRecord(
            MissionRecordUpdateRequest request, Long recordId) {
        final Member member = memberUtil.getCurrentMember();
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        validateMissionRecordUserMismatch(missionRecord.getMission(), member);

        missionRecord.updateMissionRecord(request.remark());
        return MissionRecordUpdateResponse.from(missionRecord);
    }

    public void deleteInProgressMissionRecord() {
        final Member currentMember = memberUtil.getCurrentMember();
        final LocalDate today = LocalDate.now();

        List<Mission> missions =
                missionRepository.findInProgressMissionsWithRecords(currentMember.getId());

        for (Mission mission : missions) {
            List<MissionRecord> records = mission.getMissionRecords();

            Optional<MissionRecord> optionalRecord =
                    records.stream()
                            .filter(record -> record.getStartedAt().toLocalDate().equals(today))
                            .findFirst();

            // 당일 수행한 미션기록이 없으면 NONE
            if (optionalRecord.isEmpty()) {
                continue;
            }

            // 당일 수행한 미션기록의 인증사진이 존재하면 COMPLETE
            if (optionalRecord.get().getUploadStatus() == ImageUploadStatus.COMPLETE) {
                continue;
            }

            // 레디스에 미션기록의 인증사진 인증 대기시간 값이 존재하면 REQUIRED
            Optional<MissionRecordTtl> missionRecordTTL =
                    missionRecordTtlRepository.findById(optionalRecord.get().getId());

            if (missionRecordTTL.isPresent()) {
                missionRecordTtlRepository.deleteById(optionalRecord.get().getId());
                mission.getMissionRecords().remove(optionalRecord.get()); // use orphanRemoval
            }
        }
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
