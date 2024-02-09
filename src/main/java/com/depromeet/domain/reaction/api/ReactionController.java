package com.depromeet.domain.reaction.api;

import com.depromeet.domain.reaction.application.ReactionService;
import com.depromeet.domain.reaction.dto.request.ReactionCreateRequest;
import com.depromeet.domain.reaction.dto.request.ReactionUpdateRequest;
import com.depromeet.domain.reaction.dto.response.ReactionCreateResponse;
import com.depromeet.domain.reaction.dto.response.ReactionUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. [리액션]", description = "리액션 관련 API")
@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @Operation(summary = "리액션 추가", description = "미션 기록에 리액션을 추가합니다.")
    @PostMapping
    public ResponseEntity<ReactionCreateResponse> reactionCreate(
            @Valid @RequestBody ReactionCreateRequest request) {
        ReactionCreateResponse response = reactionService.createReaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "리액션 수정", description = "리액션에서 선택한 이모지를 수정합니다.")
    @PutMapping("/{reactionId}")
    public ResponseEntity<ReactionUpdateResponse> reactionUpdate(
            @PathVariable Long reactionId, @Valid @RequestBody ReactionUpdateRequest request) {
        ReactionUpdateResponse response = reactionService.updateReaction(reactionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리액션 삭제", description = "미션 기록에서 리액션을 삭제합니다.")
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> reactionDelete(@PathVariable Long reactionId) {
        reactionService.deleteReaction(reactionId);
        return ResponseEntity.noContent().build();
    }
}
