package com.depromeet.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull(message = "미션 기록 아이디는 비워둘 수 없습니다.")
                @Schema(description = "미션 기록 아이디", defaultValue = "1")
                Long missionRecordId,
        @NotNull(message = "댓글 내용은 비워둘 수 없습니다.")
                @Schema(description = "댓글 내용", defaultValue = "댓글 내용입니다.")
                String content) {}
