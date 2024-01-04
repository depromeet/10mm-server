package com.depromeet.domain.missionRecord.dto.response;

import java.time.LocalDateTime;

import com.depromeet.domain.missionRecord.domain.MissionRecord;

public record MissionRecordFindResponse(

	Long missionRecordId,
	String remart,
	String imageUrl,
	int missionDay,
	LocalDateTime startedAt,
	LocalDateTime finishedAt,
	LocalDateTime createdAt
) {
	public static MissionRecordFindResponse from(MissionRecord missionRecord) {
		return new MissionRecordFindResponse(
			missionRecord.getId(),
			missionRecord.getRemark(),
			missionRecord.getImageUrl(),
			missionRecord.getCreatedAt().getDayOfMonth(),
			missionRecord.getStartedAt(),
			missionRecord.getFinishedAt(),
			missionRecord.getCreatedAt()
		);
	}
}
