package com.depromeet.domain.missionRecord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageUploadStatus {
    NONE("NONE"),
    PENDING("PENDING"),
    COMPLETE("COMPLETE");

    private final String value;
}
