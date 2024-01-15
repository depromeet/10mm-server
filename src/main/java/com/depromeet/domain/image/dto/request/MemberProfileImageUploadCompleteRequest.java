package com.depromeet.domain.image.dto.request;

import com.depromeet.domain.image.domain.ImageFileExtension;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberProfileImageUploadCompleteRequest(
        @NotNull(message = "이미지 파일의 확장자는 비워둘 수 없습니다.")
                @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        String nickname) {}
