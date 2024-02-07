package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.infra.config.oidc.OidcProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdTokenVerifier {

    private final OidcProperties oidcProperties;
    private final Map<OauthProvider, JwtDecoder> decoders =
            Map.of(
                    OauthProvider.KAKAO, buildDecoder(OauthProvider.KAKAO.getJwkSetUrl()),
                    OauthProvider.APPLE, buildDecoder(OauthProvider.APPLE.getJwkSetUrl()));

    private JwtDecoder buildDecoder(String jwkUrl) {
        return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
    }

    public OidcUser getOidcUser(String idToken, OauthProvider provider) {
        Jwt jwt = getJwt(idToken, provider);
        OidcIdToken oidcIdToken = getOidcIdToken(jwt);

        validateIssuer(oidcIdToken, provider.getIssuer());
        validateAudience(oidcIdToken, oidcProperties.getAudiences(provider));
        validateNonce(oidcIdToken, provider);

        return new DefaultOidcUser(null, oidcIdToken);
    }

    private Jwt getJwt(String idToken, OauthProvider provider) {
        return decoders.get(provider).decode(idToken);
    }

    private void validateAudience(OidcIdToken oidcIdToken, List<String> targetAudiences) {
        String idTokenAudience = oidcIdToken.getAudience().get(0);

        if (idTokenAudience == null || !targetAudiences.contains(idTokenAudience)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private void validateIssuer(OidcIdToken oidcIdToken, String targetIssuer) {
        String idTokenIssuer = oidcIdToken.getIssuer().toString();

        if (idTokenIssuer == null || !idTokenIssuer.equals(targetIssuer)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private OidcIdToken getOidcIdToken(Jwt jwt) {
        return new OidcIdToken(
                jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
    }

    private void validateNonce(OidcIdToken idToken, OauthProvider provider) {
        // TODO: 랜덤 nonce 사용하도록 개선
        String idTokenNonce = idToken.getNonce();
        String targetNonce = oidcProperties.nonce();

        // 카카오, 애플 앱 토큰의 경우 라이브러리 문제로 nonce 검증 생략
        if (isKakaoAppToken(idToken, provider) || isAppleAppToken(idToken, provider)) {
            return;
        }

        if (idTokenNonce == null || !idTokenNonce.equals(targetNonce)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private boolean isKakaoAppToken(OidcIdToken idToken, OauthProvider provider) {
        return provider == OauthProvider.KAKAO
                && idToken.getAudience().contains(oidcProperties.getKakaoAppAudience());
    }

    private boolean isAppleAppToken(OidcIdToken idToken, OauthProvider provider) {
        return provider == OauthProvider.APPLE
                && idToken.getAudience().contains(oidcProperties.getAppleAppAudience());
    }
}
