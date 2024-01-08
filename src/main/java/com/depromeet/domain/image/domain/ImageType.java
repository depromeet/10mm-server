package com.depromeet.domain.image.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {
    MISSION_RECORD("mission_record"),
    MEMBER_PROFILE("member_profile"),
    MEMBER_BACKGROUND("member_background"),
    ;
    private final String value;
}
