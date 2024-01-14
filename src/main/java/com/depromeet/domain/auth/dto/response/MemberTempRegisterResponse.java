package com.depromeet.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberTempRegisterResponse(
        @Schema(description = "임시 회원가입 멤버 ID", defaultValue = "1") Long memberId) {

    public static MemberTempRegisterResponse from(Long memberId) {
        return new MemberTempRegisterResponse(memberId);
    }
}
