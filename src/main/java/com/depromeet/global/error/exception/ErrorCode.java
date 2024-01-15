package com.depromeet.global.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SAMPLE_ERROR(HttpStatus.BAD_REQUEST, "Sample Error Message"),

    // Common
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Enum Type이 일치하지 않아 Binding에 실패하였습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP method 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류, 관리자에게 문의하세요"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),

    // Security
    AUTH_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "시큐리티 인증 정보를 찾을 수 없습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    MEMBER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 회원입니다."),
    SOCIAL_AUTHENTICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 소셜 로그인에 실패했습니다."),
    INVALID_APPLE_PRIVATE_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "애플 로그인에 필요한 비밀 키가 올바르지 않습니다."),
    PASSWORD_NOT_MATCHES(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    GUEST_MEMBER_REQUIRES_REGISTRATION(HttpStatus.UNAUTHORIZED, "게스트 회원은 회원가입을 먼저 진행해야 합니다."),

    // Mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션을 찾을 수 없습니다."),
    MISSION_VISIBILITY_NULL(HttpStatus.BAD_REQUEST, "미션 공개 여부가 null입니다."),
    MISSION_STATUS_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "미션 상태 중 매치되지 않는 미션이 있습니다."),

    // MissionRecord
    MISSION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션 기록을 찾을 수 없습니다."),
    MISSION_RECORD_USER_MISMATCH(HttpStatus.FORBIDDEN, "미션을 생성한 유저와 로그인된 계정이 일치하지 않습니다"),
    MISSION_RECORD_DURATION_OVERBALANCE(HttpStatus.BAD_REQUEST, "미션 참여 시간이 지정 된 시간보다 초과하였습니다"),
    MISSION_RECORD_UPLOAD_STATUS_IS_NOT_NONE(
            HttpStatus.BAD_REQUEST, "미션 기록의 이미지 업로드 상태가 NONE이 아닙니다."),
    MISSION_RECORD_UPLOAD_STATUS_IS_NOT_PENDING(
            HttpStatus.BAD_REQUEST, "미션 기록의 이미지 업로드 상태가 PENDING이 아닙니다."),
    MISSION_RECORD_ALREADY_EXISTS_TODAY(HttpStatus.BAD_REQUEST, "오늘 이미 작성 된 미션 기록이 존재합니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
