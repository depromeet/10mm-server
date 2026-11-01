package com.depromeet.scheduler.mission;

import com.depromeet.domain.missionRecord.application.MissionRecordExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionRecordExpirationScheduler {

    private final MissionRecordExpirationService missionRecordExpirationService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void reconcileExpiredMissionRecords() {
        int deletedCount = missionRecordExpirationService.deleteExpiredRecords();
        if (deletedCount > 0) {
            log.info("Expired mission record reconciliation deleted {} records", deletedCount);
        }
    }
}
