package com.depromeet.domain.batch.cron.mission.scheduler;

import com.depromeet.domain.mission.application.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionBatchScheduler {
    private final MissionService missionService;

    // 자정에 schedule 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updateCompleteDurationStatus() {
        log.info("DurationStatus Update batch execute");
        missionService.updateCompleteDurationStatus();
    }
}
