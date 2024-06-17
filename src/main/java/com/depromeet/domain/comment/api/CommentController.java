package com.depromeet.domain.comment.api;

import com.depromeet.domain.comment.application.CommentService;
import com.depromeet.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.domain.comment.dto.request.CommentUpdateRequest;
import com.depromeet.domain.comment.dto.response.CommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "9. [댓글]", description = "댓글 관련 API")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 추가", description = "미션 기록에 댓글을 추가합니다.")
    @PostMapping
    public ResponseEntity<CommentDto> commentCreate(@Valid CommentCreateRequest request) {
        CommentDto response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> commentUpdate(
            @PathVariable Long commentId, @Valid CommentUpdateRequest request) {
        CommentDto response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> commentDelete(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
