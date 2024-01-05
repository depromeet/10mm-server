package com.depromeet.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponse(@Schema(description = "Presigned URL") String presignedUrl) {
    public static PresignedUrlResponse from(String presignedUrl) {
        return new PresignedUrlResponse(presignedUrl);
    }
}
