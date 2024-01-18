package com.depromeet.domain.batch.cron.mission.jobs;

import org.springframework.stereotype.Component;

import com.depromeet.domain.mission.application.MissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionBatchJob {
	private final MissionService missionService;

	public void updateCompleteDurationStatus() {
		missionService.updateCompleteDurationStatus();
	}
}
