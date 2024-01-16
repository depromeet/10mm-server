package com.depromeet.domain.image.dto.request;

import com.depromeet.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberProfileImageUploadCompleteRequest(
        @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @NotNull(message = "닉네임은 비워둘 수 없습니다.")
        @Schema(description = "닉네임", defaultValue = "당근조이") String nickname) {}
