package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.infra.config.oidc.OidcProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class IdTokenVerifier {

    private final OidcProperties oidcProperties;
    private final Map<OauthProvider, PropertyBinder> properties;

    public IdTokenVerifier(OidcProperties oidcProperties) {
        this.oidcProperties = oidcProperties;
        this.properties =
                Map.of(
                        OauthProvider.KAKAO,
                        new PropertyBinder(
                                buildDecoder(oidcProperties.kakao().jwkSetUri()),
                                oidcProperties.kakao().issuer(),
                                oidcProperties.kakao().audience()),
                        OauthProvider.APPLE,
                        new PropertyBinder(
                                buildDecoder(oidcProperties.apple().jwkSetUri()),
                                oidcProperties.apple().issuer(),
                                oidcProperties.apple().audience()));
    }

    private JwtDecoder buildDecoder(String jwkUrl) {
        return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
    }

    public OidcUser getOidcUser(String idToken, OauthProvider provider) {
        Jwt jwt = getJwt(idToken, provider);
        OidcIdToken oidcIdToken = getOidcIdToken(jwt);
        validateIssuer(oidcIdToken, provider);
        validateAudience(oidcIdToken, provider);
        validateNonce(oidcIdToken);
        return new DefaultOidcUser(null, oidcIdToken);
    }

    private void validateAudience(OidcIdToken oidcIdToken, OauthProvider provider) {
        String idTokenAudience = oidcIdToken.getAudience().get(0);
        String targetAudience = properties.get(provider).audience();

        if (idTokenAudience == null || !idTokenAudience.equals(targetAudience)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private void validateIssuer(OidcIdToken oidcIdToken, OauthProvider provider) {
        String idTokenIssuer = oidcIdToken.getIssuer().toString();
        String targetIssuer = properties.get(provider).issuer();

        if (idTokenIssuer == null || !idTokenIssuer.equals(targetIssuer)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private Jwt getJwt(String idToken, OauthProvider provider) {
        JwtDecoder decoder = properties.get(provider).decoder();
        return decoder.decode(idToken);
    }

    private OidcIdToken getOidcIdToken(Jwt jwt) {
        return new OidcIdToken(
                jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
    }

    private void validateNonce(OidcIdToken idToken) {
        String idTokenNonceHash = idToken.getNonce();
        String targetNonceHash = getNonceHash(oidcProperties.nonce());

        if (idTokenNonceHash == null || !idTokenNonceHash.equals(targetNonceHash)) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private String getNonceHash(String nonce) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(nonce.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new CustomException(ErrorCode.ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    record PropertyBinder(JwtDecoder decoder, String issuer, String audience) {}
}
