package com.depromeet.domain.ranking.dto.response;

import com.depromeet.domain.ranking.domain.Ranking;
import io.swagger.v3.oas.annotations.media.Schema;

public record RankingResponse(
        @Schema(description = "사용자 ID", defaultValue = "1") Long memberId,
        @Schema(description = "번개 수", defaultValue = "1") long symbolStack,
        @Schema(description = "사용자 nickname", defaultValue = "default nickname") String nickname,
        @Schema(description = "프로필 이미지", defaultValue = "profile image url")
                String profileImageUrl) {
    public static RankingResponse from(Ranking ranking) {
        return new RankingResponse(
                ranking.getMember().getId(),
                ranking.getSymbolStack(),
                ranking.getMember().getProfile().getNickname(),
                ranking.getMember().getProfile().getProfileImageUrl());
    }
}
