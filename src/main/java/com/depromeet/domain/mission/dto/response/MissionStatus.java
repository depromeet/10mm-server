package com.depromeet.domain.mission.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionStatus {
    NONE("미완료"),
    REQUIRED("인증필요"),
    COMPLETED("완료"),
    ;

    private final String value;
}
