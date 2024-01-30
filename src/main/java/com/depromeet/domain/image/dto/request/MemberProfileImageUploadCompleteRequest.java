package com.depromeet.domain.image.dto.request;

import com.depromeet.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberProfileImageUploadCompleteRequest(
        @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @Schema(description = "닉네임", defaultValue = "당근조이") String nickname) {}
