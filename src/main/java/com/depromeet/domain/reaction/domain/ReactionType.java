package com.depromeet.domain.reaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReactionType {
    TENTEN("십분이"),
    LIKE("좋아요"),
    LOVE("하트"),
    THUMB_SUP("최고"),
    STAR("별"),
    UNICORN("유니콘"),
    SAD("슬퍼요"),
    CHILLING("휴식"),
    BOOK("책"),
    FOLDED_HAND("모은 손"),
    LAPTOP("노트북"),
    MUSIC("음악"),
    MUSCLE("근육"),
    GUN("총");

    private final String value;
}
