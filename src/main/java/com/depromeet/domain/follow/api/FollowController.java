package com.depromeet.domain.follow.api;

import com.depromeet.domain.follow.application.FollowService;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.domain.follow.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

    @GetMapping("/{targetId}")
    @Operation(
            summary = "타인의 팔로우 카운트 확인",
            description = "타인의 팔로잉/팔로워 카운트와 내가 타인을 팔로우를 하고있는지 확인합니다.")
    public FollowFindTargetInfoResponse followFindTarget(@PathVariable Long targetId) {
        return followService.findTargetFollowInfo(targetId);
    }

    @GetMapping("/me")
    @Operation(summary = "나의 팔로우 카운트 확인", description = "나의 팔로잉/팔로워 카운트를 확인합니다.")
    public FollowFindMeInfoResponse followFindMe() {
        return followService.findMeFollowInfo();
    }

    @GetMapping("/members")
    @Operation(
            summary = "내가 팔로우 한 유저 정보(id, 닉네임, 프로필) 리스트 조회",
            description = "팔로우 한 유저들 정보를 조회합니다.")
    public List<MemberFollowedResponse> followedUserFindAll() {
        return followService.findAllFollowedMember();
    }

    @GetMapping("/{targetId}/list")
    @Operation(summary = "팔로잉, 팔로워 유저 리스트를 반환합니다", description = "팔로잉, 팔로워 유저들을 반환합니다.")
    public FollowListResponse followList(@PathVariable Long targetId) {
        return followService.findFollowList(targetId);
    }

    @DeleteMapping({"/{targetId}"})
    @Operation(summary = "팔로워 삭제", description = "내 팔로워 목록 중 targetId로 팔로워를 삭제합니다.")
    public ResponseEntity<FollowerDeletedResponse> followerDelete(@PathVariable Long targetId) {
        return ResponseEntity.ok(followService.deleteFollower(targetId));
    }
}
