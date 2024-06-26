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
import com.depromeet.domain.missionRecord.dto.response.MissionStatisticsResponse;
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
    private static final long MAX_DURATION_HOUR = 24;

    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRecordTtlRepository missionRecordTtlRepository;

    public MissionRecordCreateResponse createMissionRecord(MissionRecordCreateRequest request) {
        long diffHour = Duration.between(request.startedAt(), request.finishedAt()).toHours();
        validateMissionRecordDurationOverTime(diffHour);

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

    private void validateMissionRecordDurationOverTime(long diffHour) {
        if (diffHour >= MAX_DURATION_HOUR) {
            throw new CustomException(ErrorCode.MISSION_RECORD_DURATION_OVERTIME);
        }
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
        return MissionRecordFindOneResponse.from(missionRecord);
    }

    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse findAllMissionRecord(Long missionId, YearMonth yearMonth) {
        final Member member = memberUtil.getCurrentMember();
        List<MissionRecord> missionRecords =
                missionRecordRepository.findAllByMissionIdAndYearMonth(missionId, yearMonth);
        List<MissionRecordFindResponse> missionRecordFindResponses =
                missionRecords.stream().map(MissionRecordFindResponse::from).toList();
        Mission mission = findMissionById(missionId);

        UrgingStatus urgingStatus = getUrgingStatus(mission, member);

        return MissionRecordCalendarResponse.of(
                mission.getStartedAt(),
                mission.getFinishedAt(),
                missionRecordFindResponses,
                urgingStatus);
    }

    private UrgingStatus getUrgingStatus(Mission mission, Member member) {
        if (member.getId().equals(mission.getMember().getId())
                || mission.isCompletedMissionToday()) {
            return UrgingStatus.NONE;
        }
        return UrgingStatus.URGING;
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

    @Transactional(readOnly = true)
    public MissionStatisticsResponse findMissionStatistics(Long missionId) {
        final Mission mission = findMissionById(missionId);
        final LocalDateTime startedAt = mission.getStartedAt();
        final LocalDateTime finishedAt = mission.getFinishedAt();
        final LocalDateTime today = LocalDateTime.now();

        List<MissionRecord> missionRecords =
                missionRecordRepository.findAllByCompletedMission(missionId);
        LocalDateTime endedAt = finishedAt.isBefore(today) ? finishedAt : today;

        // 달성률
        double totalMissionAttainRate =
                calculateMissionAttainRate(missionRecords.size(), startedAt, endedAt);

        // 시간표 생성
        List<FocusMissionRecordItem> timeTable = generateRecordTimeTable(missionRecords);

        // 최대 연속성 계산
        long maxContinuousSuccessDay =
                calculateMaxContinuousSuccessDay(startedAt, finishedAt, missionRecords);

        long totalSymbolStack = 0;
        long sumDuration = 0;
        for (FocusMissionRecordItem timeOfDay : timeTable) {
            totalSymbolStack += timeOfDay.symbolStack();
            sumDuration += timeOfDay.durationMinute();
        }

        // 전체 수행 시간 (시간)
        long totalMissionHour = sumDuration / 60;

        // 전체 수행 시간 (분)
        long totalMissionMinute = sumDuration % 60;

        return MissionStatisticsResponse.of(
                totalMissionHour,
                totalMissionMinute,
                totalSymbolStack,
                maxContinuousSuccessDay,
                missionRecords.size(),
                totalMissionAttainRate,
                startedAt,
                finishedAt,
                timeTable);
    }

    private List<FocusMissionRecordItem> generateRecordTimeTable(
            List<MissionRecord> missionRecords) {
        return missionRecords.stream().map(FocusMissionRecordItem::from).toList();
    }

    private long calculateMaxContinuousSuccessDay(
            LocalDateTime startedAt, LocalDateTime finishedAt, List<MissionRecord> missionRecords) {
        if (missionRecords.isEmpty()) return 0;

        long maxContinuousSuccessDay = 0;
        long continuousSuccessDay = 0;

        LocalDate currentDate = startedAt.toLocalDate();

        // 시작일부터 종료일까지의 각 날짜에 대해 확인합니다.
        while (!currentDate.isAfter(finishedAt.toLocalDate())) {
            boolean isSuccessDay = false;

            // 현재 날짜와 일치하는 미션 기록이 있는지 확인합니다.
            for (MissionRecord missionRecord : missionRecords) {
                LocalDate recordDate = missionRecord.getStartedAt().toLocalDate();

                // 현재 날짜와 기록의 시작 날짜가 일치 확인
                if (currentDate.isEqual(recordDate)) {
                    isSuccessDay = true;
                    break;
                }
            }

            if (isSuccessDay) {
                // 연속하는 성공일이면 카운트를 증가
                continuousSuccessDay++;
            } else {
                // 연속하는 성공일이 끊긴 경우, 최대 연속 성공일을 갱신하고 초기화
                maxContinuousSuccessDay = Math.max(continuousSuccessDay, maxContinuousSuccessDay);
                continuousSuccessDay = 0;
            }

            // 다음 날짜로 이동
            currentDate = currentDate.plusDays(1);
        }

        // 마지막 연속성을 검사하고 최대값을 반환
        return Math.max(continuousSuccessDay, maxContinuousSuccessDay);
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

    private double calculateMissionAttainRate(
            long completeSize, LocalDateTime startedAt, LocalDateTime endedAt) {
        long totalSize = Duration.between(startedAt, endedAt).toDays() + DAYS_ADJUSTMENT;
        return Math.round((double) completeSize / totalSize * 1000) / 10.0;
    }
}
