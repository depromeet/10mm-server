package com.depromeet.infra.config.jwt;

import com.depromeet.global.security.CustomRequestEntityConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.apple")
public record AppleProperties(String clientId, String clientSecret, String clientName) {
    /**
     * clientSecret 필드에는 실제 clientSecret이 들어있지 않음. 따라서 appleProperties.clientSecret()를 호출하면 안됨.
     *
     * <p>대신 clientSecret 생성에 필요한 private key, key id, team id가 순서대로들어있음 각 프로퍼티를 '|'로 구분하여 파싱하여
     * 사용함. @See {@link CustomRequestEntityConverter#convert}
     */
    private static final String APPLE_URL = "https://appleid.apple.com";

    public String privateKey() {
        return clientSecret.split("\\|")[0];
    }

    public String keyId() {
        return clientSecret.split("\\|")[1];
    }

    public String teamId() {
        return clientSecret.split("\\|")[2];
    }

    public String audience() {
        return APPLE_URL;
    }
}
