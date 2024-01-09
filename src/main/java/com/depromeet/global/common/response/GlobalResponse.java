package com.depromeet.global.common.response;

import java.time.LocalDateTime;

public record GlobalResponse(boolean success, int status, Object data, LocalDateTime timestamp) {
    public static GlobalResponse success(int status, Object data) {
        return new GlobalResponse(true, status, data, LocalDateTime.now());
    }

    public static GlobalResponse fail(int status, Object data) {
        return new GlobalResponse(false, status, data, LocalDateTime.now());
    }
}
