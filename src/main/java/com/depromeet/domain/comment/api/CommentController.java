package com.depromeet.domain.comment.api;

import com.depromeet.domain.comment.application.CommentService;
import com.depromeet.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.domain.comment.dto.response.CommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "7. [댓글]", description = "댓글 관련 API")
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
}
