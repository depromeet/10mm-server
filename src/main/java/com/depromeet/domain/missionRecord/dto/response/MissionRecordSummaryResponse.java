package com.depromeet.domain.missionRecord.dto.response;

import com.depromeet.domain.missionRecord.domain.MissionRecord;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordSummaryResponse(
	@Schema(description = "번개 수", defaultValue = "1") long stack,
	@Schema(description = "전체 누적 시간", defaultValue = "1") long totalMissionTime,
	@Schema(description = "총 미션 달성률", defaultValue = "1.1") long totalMissionRate
) {
	public static MissionRecordSummaryResponse from(MissionRecord missionRecord) {
		return new MissionRecordSummaryResponse(

		);
	}
}
