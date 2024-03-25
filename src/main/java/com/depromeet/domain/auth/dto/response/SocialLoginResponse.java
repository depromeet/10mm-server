package com.depromeet.domain.auth.dto.response;

import com.depromeet.domain.auth.domain.LandingStatus;
import com.depromeet.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginResponse(
        @Schema(description = "멤버 ID", defaultValue = "1") Long memberId,
        @Schema(description = "엑세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken,
        @Schema(description = "게스트 여부", defaultValue = "false") boolean isGuest,
        @Schema(description = "랜딩 상태", defaultValue = "TO_MAIN") LandingStatus landingStatus) {

    public static SocialLoginResponse of(
            Member member, TokenPairResponse tokenPairResponse, LandingStatus landingStatus) {
        return new SocialLoginResponse(
                member.getId(),
                tokenPairResponse.accessToken(),
                tokenPairResponse.refreshToken(),
                false,
                landingStatus);
    }
}
