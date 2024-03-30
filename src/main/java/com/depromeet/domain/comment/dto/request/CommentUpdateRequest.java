package com.depromeet.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(
        @NotNull(message = "댓글 내용은 비워둘 수 없습니다.")
                @Schema(description = "댓글 내용", defaultValue = "댓글 내용입니다.")
                String content) {}
