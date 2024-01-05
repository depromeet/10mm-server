package com.depromeet.domain.image.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {
    MISSION_RECORD("mission_record"),
    ;
    private final String value;
}
