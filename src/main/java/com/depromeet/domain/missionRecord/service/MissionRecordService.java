package com.depromeet.domain.missionRecord.service;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
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

    public void createMissionRecord(MissionRecordCreateRequest request) {
        final Mission mission =
                missionRepository
                        .findByMissionId(request.missionId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        final Member member = memberUtil.getCurrentMember();

        validateMissionRecordUserMismatch(mission, member);

        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        request.durationMin(),
                        request.durationSec(),
                        request.startedAt(),
                        request.finishedAt(),
                        mission);

        missionRecordRepository.save(missionRecord);
    }

    private void validateMissionRecordUserMismatch(Mission mission, Member member) {
        if (member.getId().equals(mission.getMember().getId())) {
            throw new CustomException(ErrorCode.MISSION_RECORD_USER_MISMATCH);
        }
    }
}
