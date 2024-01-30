package com.depromeet.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record NicknameUpdateRequest(
        @NotNull(message = "닉네임은 비워둘 수 없습니다.")
                @Schema(description = "회원 닉네임", defaultValue = "nickname")
                String nickname) {}
