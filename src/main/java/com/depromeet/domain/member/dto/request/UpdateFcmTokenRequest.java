package com.depromeet.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateFcmTokenRequest(
	@Schema(description = "FCM 토큰", defaultValue = "fcm-token-value")
	String fcmToken
) {
}
