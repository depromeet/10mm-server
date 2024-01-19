package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DurationStatus {
    IN_PROGRESS("진행 중"),
    FINISHED("종료");

    private final String value;
}
