package com.depromeet.domain.comment.dto;

import com.depromeet.domain.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record CommentDto(
        @Schema(description = "댓글 ID", defaultValue = "1") Long commentId,
        @Schema(description = "댓글 내용", defaultValue = "댓글 내용입니다.") String content,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd HH:mm:ss",
                        timezone = "Asia/Seoul")
                @Schema(
                        description = "댓글 작성 시간",
                        defaultValue = "2024-03-25 00:00:00",
                        type = "string")
                LocalDateTime createdAt) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(comment.getId(), comment.getContent(), comment.getCreatedAt());
    }
}
