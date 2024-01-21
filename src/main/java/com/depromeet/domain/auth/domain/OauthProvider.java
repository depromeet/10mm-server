package com.depromeet.domain.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OauthProvider {
    KAKAO("kakao"),
    APPLE("apple"),
    ;

    private final String provider;
}
