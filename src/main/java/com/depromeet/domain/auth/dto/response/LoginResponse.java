package com.depromeet.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "엑세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken) {

    public static LoginResponse from(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken);
    }
}
