package com.depromeet.global.error.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	SAMPLE_ERROR(HttpStatus.BAD_REQUEST, "Sample Error Message"),
	;

	private final HttpStatus status;
	private final String message;
}
