package com.depromeet.domain.auth.domain;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OauthProvider {
    KAKAO,
    APPLE,
    ;
}
