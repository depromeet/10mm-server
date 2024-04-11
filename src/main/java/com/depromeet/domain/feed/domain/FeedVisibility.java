package com.depromeet.domain.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedVisibility {
    ALL("전체 피드"),
    FOLLOWING("팔로잉 피드"),
    ;

    private final String value;
}
