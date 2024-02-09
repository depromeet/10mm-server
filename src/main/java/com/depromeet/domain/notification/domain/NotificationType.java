package com.depromeet.domain.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    FOLLOW("팔로우"),
    MISSION_URGING("재촉하기"),
    ;

    private final String value;
}
