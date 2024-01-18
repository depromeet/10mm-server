package com.depromeet.domain.batch.cron.mission.jobs;

import com.depromeet.domain.mission.application.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionBatchJob {
    private final MissionService missionService;

    public void updateCompleteDurationStatus() {
        missionService.updateCompleteDurationStatus();
    }
}
