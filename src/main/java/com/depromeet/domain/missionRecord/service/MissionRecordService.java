package com.depromeet.domain.missionRecord.service;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

	public List<MissionRecordFindResponse> findAllMissionRecord(Long missionId, String yearMonth) {
		// "yyyy-MM" 형식인지 검증
		validateYearMonthFormat(yearMonth);

		// year와 month를 분리
		String[] parts = yearMonth.split("-");
		String year = parts[0];
		String month = parts[1];

		List<MissionRecord> missionRecords = missionRecordRepository.findAllByMissionId(missionId, year, month);
		return missionRecords.stream().map(MissionRecordFindResponse::from).toList();
	}

    private Mission findMission(MissionRecordCreateRequest request) {
        return missionRepository
                .findByMissionId(request.missionId())
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


	private void validateYearMonthFormat(String yearMonth) {
		try {
			// 파싱 가능한지 확인
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			LocalDate.parse(yearMonth + "-01", formatter);
		} catch (DateTimeParseException e) {
			throw new CustomException(ErrorCode.MiSSION_RECORD_YEAR_MONTH_INVALID);
		}
	}
}
