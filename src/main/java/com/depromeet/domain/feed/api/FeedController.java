package com.depromeet.domain.feed.api;

import com.depromeet.domain.feed.application.FeedService;
import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "6. [피드]", description = "피드 관련 API입니다.")
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "피드 탭", description = "피드 탭을 조회합니다.")
    @GetMapping("/me")
    public List<FeedOneResponse> feedFindAll() {
        return feedService.findAllFeed();
    }

    @Operation(summary = "프로필 피드", description = "피드 탭을 조회합니다.")
    @GetMapping("/{targetId}")
    public List<FeedOneByProfileResponse> feedFindAllByTargetId(@PathVariable Long targetId) {
        return feedService.findAllFeedByTargetId(targetId);
    }
}