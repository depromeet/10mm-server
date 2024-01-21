package com.depromeet.global.common.constants;

public final class SecurityConstants {

    public static final String TOKEN_ROLE_NAME = "role";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ACCESS_TOKEN_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    public static final String REGISTER_REQUIRED_HEADER = "Registration-Required";
    public static final String KAKAO_JWK_URL = "https://kauth.kakao.com/.well-known/jwks.json";
    public static final String APPLE_JWK_URL = "https://appleid.apple.com/auth/keys";
    public static final String KAKAO_ISSUER = "https://kauth.kakao.com";
    public static final String APPLE_ISSUER = "https://appleid.apple.com";

    private SecurityConstants() {}
}
