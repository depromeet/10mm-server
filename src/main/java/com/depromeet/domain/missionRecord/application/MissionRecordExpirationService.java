package com.depromeet.domain.missionRecord.application;

import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionRecordExpirationService {

    private final MissionRecordRepository missionRecordRepository;
    private final Clock clock;

    @Transactional
    public int deleteExpiredRecord(Long recordId) {
        return missionRecordRepository.deleteExpiredIncompleteRecord(
                recordId, LocalDateTime.now(clock));
    }

    @Transactional
    public int deleteExpiredRecords() {
        return missionRecordRepository.deleteExpiredIncompleteRecords(LocalDateTime.now(clock));
    }
}
