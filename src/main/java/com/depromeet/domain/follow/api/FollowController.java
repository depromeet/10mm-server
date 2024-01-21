package com.depromeet.domain.follow.api;

import com.depromeet.domain.follow.application.FollowService;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. [팔로우]", description = "팔로우 관련 API입니다.")
@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping
    @Operation(summary = "팔로우 추가", description = "팔로우를 추가합니다.")
    public ResponseEntity<Void> followCreate(@Valid @RequestBody FollowCreateRequest request) {
        followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(summary = "팔로우 취소", description = "팔로우를 취소합니다.")
    public void followDelete(@Valid @RequestBody FollowDeleteRequest request) {
        followService.deleteFollow(request);
    }
}
