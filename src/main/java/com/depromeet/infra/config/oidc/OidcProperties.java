package com.depromeet.infra.config.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc")
public record OidcProperties(String nonce, Kakao kakao, Apple apple) {
    public record Kakao(String jwkSetUrl, String issuer, String audience) {}

    public record Apple(String jwkSetUrl, String issuer, String audience) {}
}
