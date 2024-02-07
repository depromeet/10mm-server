package com.depromeet.domain.auth.dto;

public record RefreshTokenDto(Long memberId, String tokenValue, Long ttl) {}
