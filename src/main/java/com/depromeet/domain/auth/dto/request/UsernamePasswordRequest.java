package com.depromeet.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UsernamePasswordRequest(
        @NotNull(message = "아이디는 비워둘 수 없습니다.")
                @Schema(description = "회원 아이디", defaultValue = "username")
                String username,
        @NotNull(message = "비밀번호는 비워둘 수 없습니다.")
                @Schema(description = "회원 비밀번호", defaultValue = "password")
                String password) {}
