package com.depromeet.domain.batch.cron.mission.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.depromeet.domain.batch.cron.mission.jobs.MissionBatchJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionBatchScheduler {
	private final MissionBatchJob missionBatchJob;

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void updateCompleteDurationStatus() {
		log.info("DurationStatus Update batch execute");
		missionBatchJob.updateCompleteDurationStatus();
	}
}
