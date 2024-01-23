package com.depromeet.infra.config.oidc;

import com.depromeet.domain.auth.domain.OauthProvider;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record OidcProperties(String nonce, Map<OauthProvider, List<Audience>> audience) {
    record Audience(String key, AudienceType type) {}

    public enum AudienceType {
        WEB,
        APP,
        ALL
    }

    public List<String> getAudiences(OauthProvider provider) {
        return audience.get(provider).stream().map(Audience::key).toList();
    }

    public String getKakaoAppAudience() throws NoSuchElementException {
        return audience.get(OauthProvider.KAKAO).stream()
                .filter(audience -> audience.type() == AudienceType.APP)
                .findFirst()
                .map(Audience::key)
                .orElseThrow();
    }
}
