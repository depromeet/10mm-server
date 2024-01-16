package com.depromeet.global.util;

import com.depromeet.infra.config.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final JwtProperties jwtProperties;

    public void addTokenCookies(
            HttpServletResponse response, String accessToken, String refreshToken) {
        HttpHeaders headers = generateTokenCookies(accessToken, refreshToken);
        headers.forEach((key, value) -> response.addHeader(key, value.get(0)));
    }

    public HttpHeaders generateTokenCookies(String accessToken, String refreshToken) {

        String sameSite = determineSameSitePolicy();

        ResponseCookie accessTokenCookie =
                ResponseCookie.from("accessToken", accessToken)
                        .path("/")
                        .maxAge(jwtProperties.accessTokenExpirationTime())
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(false)
                        .build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refreshToken", refreshToken)
                        .path("/")
                        .maxAge(jwtProperties.refreshTokenExpirationTime())
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(true)
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
