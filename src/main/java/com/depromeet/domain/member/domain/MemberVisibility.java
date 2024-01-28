package com.depromeet.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberVisibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE");

    private final String value;
}
