package com.depromeet.domain.mission.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionSymbolStackResponse(
        @Schema(description = "사용자 ID", defaultValue = "1") Long memberId,
        @Schema(description = "번개 수", defaultValue = "1") long symbolStack) {
    public static MissionSymbolStackResponse of(Long memberId, long symbolStack) {
        return new MissionSymbolStackResponse(memberId, symbolStack);
    }
}
