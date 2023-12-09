package com.depromeet.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;
}
