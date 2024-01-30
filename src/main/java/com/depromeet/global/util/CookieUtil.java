package com.depromeet.global.util;

import static com.depromeet.global.common.constants.SecurityConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.depromeet.global.common.constants.SecurityConstants.REFRESH_TOKEN_COOKIE_NAME;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final SpringEnvironmentUtil springEnvironmentUtil;

    public HttpHeaders generateTokenCookies(String accessToken, String refreshToken) {

        String sameSite = determineSameSitePolicy();

        ResponseCookie accessTokenCookie =
                ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                        .path("/")
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(false)
                        .build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                        .path("/")
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(false)
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return headers;
    }

    private String determineSameSitePolicy() {
        if (springEnvironmentUtil.isProdProfile()) {
            return "Strict";
        }
        return "None";
    }
}
