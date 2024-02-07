package com.depromeet.domain.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    FOLLOW("팔로우"),
    ;

    private final String value;
}
