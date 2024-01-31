package com.depromeet.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record IdTokenRequest(
        @NotNull(message = "Id Token은 비워둘 수 없습니다.") @Schema(description = "Id Token")
                String idToken) {}
