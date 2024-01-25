package com.depromeet.domain.mission.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionSymbolStackResponse(
	@Schema(description = "번개 수", defaultValue = "1") long symbolStack
) {
	public static MissionSymbolStackResponse of(
		long symbolStack) {
		return new MissionSymbolStackResponse(symbolStack);
	}
}
