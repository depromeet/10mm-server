package com.depromeet.domain.auth.application;

import static com.depromeet.global.common.constants.SecurityConstants.APPLE_JWK_URL;
import static com.depromeet.global.common.constants.SecurityConstants.KAKAO_JWK_URL;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class IdTokenVerifier {

    private static final Map<OauthProvider, JwtDecoder> decoders =
            Map.of(
                    OauthProvider.KAKAO, buildDecoder(KAKAO_JWK_URL),
                    OauthProvider.APPLE, buildDecoder(APPLE_JWK_URL));

    @Value("${oidc.nonce}")
    private String targetNonce;

    private static JwtDecoder buildDecoder(String jwkSetUri) {
        SignatureAlgorithm algorithm = SignatureAlgorithm.RS256;
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).jwsAlgorithm(algorithm).build();
    }

    public OidcUser getOidcUser(String idToken, OauthProvider provider) {
        Jwt jwt = getJwt(idToken, provider);
        OidcIdToken oidcIdToken = getOidcIdToken(jwt);
        validateNonce(oidcIdToken);
        return new DefaultOidcUser(null, oidcIdToken);
    }

    private Jwt getJwt(String idToken, OauthProvider provider) {
        JwtDecoder decoder = decoders.get(provider);
        return decoder.decode(idToken);
    }

    private OidcIdToken getOidcIdToken(Jwt jwt) {
        return new OidcIdToken(
                jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
    }

    private void validateNonce(OidcIdToken idToken) {
        String idTokenNonceHash = idToken.getNonce();
        String targetNonceHash = getNonceHash(targetNonce);

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
}
