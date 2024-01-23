package com.depromeet.domain.auth.domain;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OauthProvider {
    KAKAO(KAKAO_JWK_SET_URL, KAKAO_ISSUER),
    APPLE(APPLE_JWK_SET_URL, APPLE_ISSUER),
    ;

    private final String jwkSetUrl;
    private final String issuer;
}
