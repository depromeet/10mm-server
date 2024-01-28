package com.depromeet.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginResponse(
        @Schema(description = "엑세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken) {

    public static SocialLoginResponse from(TokenPairResponse tokenPairResponse) {
        return new SocialLoginResponse(
                tokenPairResponse.accessToken(), tokenPairResponse.refreshToken());
    }
}
