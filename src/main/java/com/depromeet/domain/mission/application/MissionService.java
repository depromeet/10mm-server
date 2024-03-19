package com.depromeet.domain.mission.application;

import com.depromeet.domain.follow.dao.MemberRelationRepository;
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
import com.depromeet.domain.missionRecord.dto.response.MissionRecordSummaryResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionRecordTtlRepository missionRecordTtlRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final MemberUtil memberUtil;
    private final SecurityUtil securityUtil;

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
        final Member currentMember = memberUtil.getCurrentMember();
        final LocalDate today = LocalDate.now();

        List<Mission> missions =
                missionRepository.findInProgressMissionsWithRecords(currentMember.getId());

        List<MissionFindAllResponse> results = new ArrayList<>();
        for (Mission mission : missions) {
            List<MissionRecord> records = mission.getMissionRecords();

            Optional<MissionRecord> optionalRecord =
                    records.stream()
                            .filter(record -> record.getStartedAt().toLocalDate().equals(today))
                            .findFirst();

            // 당일 수행한 미션기록이 없으면 NONE
            if (optionalRecord.isEmpty()) {
                results.add(MissionFindAllResponse.of(mission, MissionStatus.NONE, null, null));
                continue;
            }

            // 당일 수행한 미션기록의 인증사진이 존재하면 COMPLETE
            if (optionalRecord.get().getUploadStatus() == ImageUploadStatus.COMPLETE) {
                results.add(
                        MissionFindAllResponse.of(
                                mission,
                                MissionStatus.COMPLETED,
                                null,
                                optionalRecord.get().getId()));
                continue;
            }

            // 레디스에 미션기록의 인증사진 인증 대기시간 값이 존재하면 REQUIRED
            Optional<MissionRecordTtl> missionRecordTTL =
                    missionRecordTtlRepository.findById(optionalRecord.get().getId());

            if (missionRecordTTL.isPresent()) {
                results.add(
                        MissionFindAllResponse.of(
                                mission,
                                MissionStatus.REQUIRED,
                                missionRecordTTL.get().getTtlFinishedAt(),
                                optionalRecord.get().getId()));
                continue;
            }

            throw new CustomException(ErrorCode.MISSION_STATUS_MISMATCH);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public MissionRecordSummaryResponse findSummaryMissionRecord() {
        final Member member = memberUtil.getCurrentMember();
        List<Mission> missions = missionRepository.findMissionsWithRecords(member.getId());
        List<MissionRecord> completedMissionRecords = findCompletedMissionRecords(missions);
        final LocalDateTime today = LocalDateTime.now();

        // 번개 stack 누적할 변수 선언
        long symbolStack = symbolStackCalculate(completedMissionRecords);

        // 미션 수행 일수 계산으로 (today - 생성알자) 일수 계산하여 AttainRate에 활용
        long totalMissionDay =
                missions.stream()
                        .mapToLong(
                                mission ->
                                        Duration.between(mission.getStartedAt(), today).toDays()
                                                + 1)
                        .sum();
        // Duration을 초로 바꾸고 합산
        long sumDuration =
                completedMissionRecords.stream()
                        .mapToLong(missionRecord -> missionRecord.getDuration().toSeconds())
                        .reduce(0, Long::sum);

        /* 합산한 시간의 시간과 분 구하기
         * 시간 : 초 / 3600
         * 분 : (초 % 3600) / 60
         */
        long totalMissionHour = sumDuration / 3600;
        long totalMissionMinute = (sumDuration % 3600) / 60;

        // 달성률 계산
        // TODO: 달성률 계산에 따른 테스트 코드 추가
        double totalMissionAttainRate =
                calculateMissionAttainRate(completedMissionRecords.size(), totalMissionDay);

        return MissionRecordSummaryResponse.from(
                symbolStack, totalMissionHour, totalMissionMinute, totalMissionAttainRate);
    }

    // 친구 미션 목록
    public FollowMissionFindAllResponse findAllFollowMissions(Long targetId) {
        final Member sourceMember = memberUtil.getCurrentMember();
        final Member targetMember = memberUtil.getMemberByMemberId(targetId);
        final LocalDate today = LocalDate.now();

        boolean existMemberRelation =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        sourceMember.getId(), targetMember.getId());

        List<Mission> missions =
                missionRepository.findMissionsWithRecordsByRelations(
                        targetMember.getId(), existMemberRelation);

        List<MissionRecord> completedMissionRecords = findCompletedMissionRecords(missions);
        // 번개 stack 누적할 변수 선언
        long symbolStack = symbolStackCalculate(completedMissionRecords);

        List<MissionFindAllResponse> findAllResponses = new ArrayList<>();
        for (Mission mission : missions) {
            List<MissionRecord> records = mission.getMissionRecords();

            Optional<MissionRecord> optionalRecord =
                    records.stream()
                            .filter(record -> record.getStartedAt().toLocalDate().equals(today))
                            .findFirst();

            // 기본 값 NONE으로 지정 후
            MissionStatus missionStatus = MissionStatus.NONE;
            Long missionRecordId = null;
            // 당일 수행한 미션 기록의 인증 사진이 존재하면 COMPLETE
            if (optionalRecord.isPresent()
                    && (optionalRecord.get().getUploadStatus() == ImageUploadStatus.COMPLETE)) {
                missionRecordId = optionalRecord.get().getId();
                missionStatus = MissionStatus.COMPLETED;
            }

            findAllResponses.add(
                    MissionFindAllResponse.of(mission, missionStatus, null, missionRecordId));
        }

        // 완료된 미션이 상단으로
        findAllResponses.sort(
                Comparator.comparing(
                        response -> response.missionStatus() == MissionStatus.COMPLETED ? 0 : 1));

        return FollowMissionFindAllResponse.of(symbolStack, findAllResponses);
    }

    @Transactional(readOnly = true)
    public MissionSymbolStackResponse findMissionSymbolStack(Long memberId) {
        final Member currentMember = memberUtil.getMemberByMemberId(memberId);
        List<Mission> missions = missionRepository.findMissionsWithRecords(currentMember.getId());
        List<MissionRecord> completedMissionRecords = findCompletedMissionRecords(missions);

        // 번개 stack 누적할 변수 선언
        long symbolStack = symbolStackCalculate(completedMissionRecords);
        return MissionSymbolStackResponse.of(symbolStack);
    }

    @Transactional(readOnly = true)
    public List<FinishedMissionResponse> findAllFinishedMission() {
        Long currentMemberId = securityUtil.getCurrentMemberId();

        List<Mission> finishedMissions = missionRepository.findAllFinishedMission(currentMemberId);

        return finishedMissions.stream()
                .map(
                        mission -> {
                            long totalMissionDay =
                                    Duration.between(
                                                            mission.getStartedAt(),
                                                            mission.getFinishedAt())
                                                    .toDays()
                                            + 1;
                            long completeCount =
                                    mission.getMissionRecords().stream()
                                            .filter(
                                                    missionRecord ->
                                                            missionRecord
                                                                    .getUploadStatus()
                                                                    .equals(
                                                                            ImageUploadStatus
                                                                                    .COMPLETE))
                                            .count();

                            return FinishedMissionResponse.of(
                                    mission,
                                    calculateMissionAttainRate(completeCount, totalMissionDay));
                        })
                .toList();
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
				missionCreateRequest.remindedAt(),
                member);
    }

    /* 달성률
    계산식: (완료된 미션 수 / 전체 미션 수 * 1000.0) / 10.0
    소수점 첫 째 자리까지
     */
    private double calculateMissionAttainRate(long completeSize, long totalSize) {
        return Math.round((double) completeSize / totalSize * 1000) / 10.0;
    }

    public void updateFinishedDurationStatus() {
        final LocalDateTime today = LocalDateTime.now().withSecond(0).withNano(0);
        missionRepository.updateFinishedDurationStatus(today);
    }

    // 번개 stack 누적 메서드
    public long symbolStackCalculate(List<MissionRecord> missionRecords) {
        return missionRecords.stream()
                .mapToLong(missionRecord -> missionRecord.getDuration().toMinutes() / 10)
                .sum();
    }

    // 업로드 완료 미션 기록 리스트
    public List<MissionRecord> findCompletedMissionRecords(List<Mission> missions) {
        return missions.stream()
                .flatMap(mission -> mission.getMissionRecords().stream())
                .filter(
                        missionRecord ->
                                missionRecord.getUploadStatus() == ImageUploadStatus.COMPLETE)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionSummaryListResponse findSummaryList(LocalDate date) {
        final Member currentMember = memberUtil.getCurrentMember();

        List<Mission> missions =
                missionRepository.findMissionsWithRecordsByDate(date, currentMember.getId());

        List<MissionSummaryItem> result =
                missions.stream()
                        .map(mission -> getMissionSummaryItem(mission, date))
                        .sorted(
                                Comparator.comparing(MissionSummaryItem::missionStatus)
                                        .reversed()
                                        .thenComparing(
                                                Comparator.comparing(MissionSummaryItem::finishedAt)
                                                        .reversed()))
                        .collect(Collectors.toList());

        long missionAllCount = missions.size();
        long missionCompleteCount =
                result.stream()
                        .filter(
                                missionSummaryItem ->
                                        missionSummaryItem.missionStatus()
                                                == MissionStatus.COMPLETED)
                        .count();
        long missionNoneCount = missionAllCount - missionCompleteCount;
        return MissionSummaryListResponse.of(
                missionAllCount, missionCompleteCount, missionNoneCount, result);
    }

    private static MissionSummaryItem getMissionSummaryItem(Mission mission, LocalDate date) {
        boolean isCompleted =
                mission.getMissionRecords().stream()
                        .anyMatch(
                                missionRecord ->
                                        missionRecord.getUploadStatus()
                                                        == ImageUploadStatus.COMPLETE
                                                && missionRecord
                                                        .getStartedAt()
                                                        .toLocalDate()
                                                        .equals(date));
        return isCompleted
                ? MissionSummaryItem.of(mission, MissionStatus.COMPLETED)
                : MissionSummaryItem.of(mission, MissionStatus.NONE);
    }
}
