package com.depromeet.global.config.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public record GlobalResponse(boolean success, int status, Object data, LocalDateTime timestamp) {
    public static GlobalResponse of(int status, Object data) {
        return new GlobalResponse(true, status, data, LocalDateTime.now());
    }
}
