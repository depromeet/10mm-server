package com.depromeet.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Slice;

public record FeedSliceResponse(
        @Schema(description = "피드 데이터") List<FeedOneResponse> content,
        @Schema(description = "마지막 페이지 여부") Boolean last) {
    public static FeedSliceResponse from(Slice<FeedOneResponse> feedResponses) {
        return new FeedSliceResponse(feedResponses.getContent(), feedResponses.isLast());
    }
}
