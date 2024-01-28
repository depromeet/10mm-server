package com.depromeet.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnvironmentConstants {
    PROD("prod"),
    DEV("dev"),
    LOCAL("local"),
    ;

    private String value;
}
