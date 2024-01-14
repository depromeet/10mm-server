package com.depromeet.global.security;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_SECRET;

import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.infra.config.jwt.AppleProperties;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomRequestEntityConverterV2
        extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    public static final int APPLE_CLIENT_SECRET_EXIPRE_DURATION = 30;
    private final AppleProperties appleProperties;

    @Override
    protected MultiValueMap<String, String> createParameters(
            OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        MultiValueMap<String, String> parameters =
                super.createParameters(authorizationCodeGrantRequest);

        if (isAppleRequest(authorizationCodeGrantRequest)) {
            changeClientSecretParamToGeneratedValue(parameters);
        }

        return parameters;
    }

    private boolean isAppleRequest(
            OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        String registrationId =
                authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
        return registrationId.equals(appleProperties.clientName());
    }

    private void changeClientSecretParamToGeneratedValue(MultiValueMap<String, String> parameters) {
        String realClientSecret = generateClientSecret();
        parameters.set(CLIENT_SECRET, realClientSecret);
    }

    public String generateClientSecret() {
        LocalDateTime now = LocalDateTime.now();

        Date expireAt =
                Date.from(
                        now.plusDays(APPLE_CLIENT_SECRET_EXIPRE_DURATION)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, appleProperties.keyId())
                .setIssuer(appleProperties.teamId())
                .setIssuedAt(issuedAt)
                .setExpiration(expireAt)
                .setAudience(appleProperties.audience())
                .setSubject(appleProperties.clientId())
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    public PrivateKey getPrivateKey() {
        try {
            StringReader reader = new StringReader(appleProperties.privateKey());
            PEMParser pemParser = new PEMParser(reader);
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(object);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_APPLE_PRIVATE_KEY);
        }
    }
}
