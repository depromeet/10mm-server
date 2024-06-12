package com.depromeet.domain.feed.api;

import com.depromeet.domain.feed.application.FeedService;
import com.depromeet.domain.feed.domain.FeedVisibility;
import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.feed.dto.response.FeedSliceResponse;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "7. [피드]", description = "피드 관련 API입니다.")
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Deprecated
    @Operation(summary = "피드 탭", description = "피드 탭을 조회합니다.")
    @GetMapping
    public List<FeedOneResponse> feedFindAll(
            @RequestParam(value = "visibility", required = false) MissionVisibility visibility) {
        return feedService.findAllFeedByVisibility(visibility);
    }

    @Operation(summary = "피드 탭 (페이지네이션)", description = "피드 탭을 조회합니다.")
    @GetMapping("/me")
    public FeedSliceResponse feedFindByPage(
            @RequestParam int size,
            @RequestParam(required = false) Long lastId,
            @RequestParam(value = "visibility", required = false) MissionVisibility visibility) {
        return feedService.findFeed(size, lastId, visibility);
    }

    @Operation(summary = "피드 탭 V2 (페이지네이션)", description = "피드 탭을 조회합니다.")
    @GetMapping("/me/v2")
    public Slice<FeedOneResponse> feedFindByPageV2(
            @RequestParam int size,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) FeedVisibility visibility) {
        return feedService.findFeedV2(visibility, size, lastId);
    }

    @Operation(summary = "프로필 피드", description = "피드 탭을 조회합니다.")
    @GetMapping("/{memberId}")
    public List<FeedOneByProfileResponse> feedFindAllByTargetId(@PathVariable Long memberId) {
        return feedService.findAllFeedByTargetId(memberId);
    }
}
