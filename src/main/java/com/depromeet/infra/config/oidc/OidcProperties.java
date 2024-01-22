package com.depromeet.infra.config.oidc;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc")
public record OidcProperties(String nonce, Kakao kakao, Apple apple) {
    public record Kakao(String jwkSetUri, String issuer, List<String> audience) {}

    public record Apple(String jwkSetUri, String issuer, List<String> audience) {}
}
