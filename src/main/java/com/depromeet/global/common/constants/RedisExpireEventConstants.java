package com.depromeet.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisExpireEventConstants {
    REDIS_EXPIRE_EVENT_PATTERN("__keyevent@*__:expired"),
    EXPIRE_EVENT_IMAGE_UPLOAD_TIME_END("EXPIRE_EVENT_IMAGE_UPLOAD_TIME_END_");
    private final String value;
}
