package com.depromeet.domain.auth.domain;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import java.util.Arrays;
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

    public static OauthProvider of(String issuer) {
        return Arrays.stream(values())
                .filter(oauthProvider -> oauthProvider.issuer.equals(issuer))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_PROVIDER_NOT_FOUND));
    }
}
