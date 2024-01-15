package com.depromeet.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UsernameCheckRequest(
        @NotNull(message = "아이디는 비워둘 수 없습니다.")
                @Schema(description = "회원 아이디", defaultValue = "username")
                String username) {}
