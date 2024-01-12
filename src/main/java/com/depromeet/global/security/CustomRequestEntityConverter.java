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
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class CustomRequestEntityConverter
        implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {
    private final AppleProperties appleProperties;
    private final OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

    public CustomRequestEntityConverter(AppleProperties appleProperties) {
        this.appleProperties = appleProperties;
        this.defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> entity = defaultConverter.convert(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        MultiValueMap<String, String> params = (MultiValueMap<String, String>) entity.getBody();

        setGeneratedClientSecretIfAppleLogin(registrationId, params);

        return new RequestEntity<>(
                params, entity.getHeaders(), entity.getMethod(), entity.getUrl());
    }

    private void setGeneratedClientSecretIfAppleLogin(
            String registrationId, MultiValueMap<String, String> params) {
        if (registrationId.equals(appleProperties.clientName())) {
            try {
                params.set(CLIENT_SECRET, generateClientSecret());
            } catch (IOException e) {
                throw new CustomException(ErrorCode.INVALID_APPLE_PRIVATE_KEY);
            }
        }
    }

    public PrivateKey getPrivateKey() throws IOException {
        PEMParser pemParser = new PEMParser(new StringReader(appleProperties.privateKey()));
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return converter.getPrivateKey(object);
    }

    public String generateClientSecret() throws IOException {
        LocalDateTime now = LocalDateTime.now();

        Date expireAt = Date.from(now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
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
}
