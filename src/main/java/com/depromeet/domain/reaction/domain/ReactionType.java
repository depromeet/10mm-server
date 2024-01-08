package com.depromeet.domain.reaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReactionType {
    PURPLE_HEART("PURPLE_HEART"),
    STAR("STAR"),
    RED_HEART("RED_HEART"),
    FIRECRACKER("FIRECRACKER"),
    UNICORN("UNICORN"),
    FIRE("FIRE"),
    EYE("EYE");

    private final String value;
}
