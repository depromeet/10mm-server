package com.depromeet.domain.reaction.api;

import com.depromeet.domain.reaction.application.ReactionService;
import com.depromeet.domain.reaction.dto.ReactionCreateRequest;
import com.depromeet.domain.reaction.dto.ReactionCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
