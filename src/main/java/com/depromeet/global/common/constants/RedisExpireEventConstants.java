package com.depromeet.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisExpireEventConstants {
    REDIS_EXPIRE_EVENT_PATTERN("__keyevent@*__:expired"),
    ;
    private final String value;
}
