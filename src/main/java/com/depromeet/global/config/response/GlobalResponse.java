package com.depromeet.global.config.response;

import java.time.LocalDateTime;

public record GlobalResponse(boolean success, int status, Object data, LocalDateTime timestamp) {
    public static GlobalResponse of(int status, Object data) {
        return new GlobalResponse(true, status, data, LocalDateTime.now());
    }
}
