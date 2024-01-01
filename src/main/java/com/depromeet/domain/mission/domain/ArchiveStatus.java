package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArchiveStatus {
    NONE("미완료"),
    REQUIRED("인증 필요"),
    ARCHIVED("완료");

    private final String value;
}
