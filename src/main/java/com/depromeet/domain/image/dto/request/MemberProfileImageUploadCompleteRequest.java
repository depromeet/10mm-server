package com.depromeet.domain.image.dto.request;

import com.depromeet.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberProfileImageUploadCompleteRequest(
        @NotNull(message = "이미지 파일의 확장자는 비워둘 수 없습니다.")
                @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @Schema(description = "닉네임", defaultValue = "당근조이") String nickname) {}
