package com.depromeet.global.error;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String message, LocalDateTime timestamp) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message, LocalDateTime.now());
    }
}
