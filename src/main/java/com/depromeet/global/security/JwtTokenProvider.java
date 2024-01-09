package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.TOKEN_ROLE_NAME;

import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.domain.token.dto.AccessToken;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.infra.config.jwt.JwtProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final Key accessTokenKey;
    private final Key refreshTokenKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessTokenKey = Keys.hmacShaKeyFor(jwtProperties.accessTokenSecret().getBytes());
        this.refreshTokenKey = Keys.hmacShaKeyFor(jwtProperties.refreshTokenSecret().getBytes());
    }

    public String generateAccessToken(Long memberId, MemberRole memberRole) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.accessTokenExpirationMilliTime());
        return buildAccessToken(memberId, memberRole, issuedAt, expiredAt);
    }

    public String generateRefreshToken(Long memberId) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.refreshTokenExpirationMilliTime());
        return buildRefreshToken(memberId, issuedAt, expiredAt);
    }

    public AccessToken parseAccessToken(String token) {
        Jws<Claims> claims = getClaims(token, accessTokenKey);

        try {
            validateDefaultClaims(claims);
            return new AccessToken(
                    Long.parseLong(claims.getBody().getSubject()),
                    MemberRole.valueOf(claims.getBody().get(TOKEN_ROLE_NAME, String.class)));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    public Long parseRefreshToken(String token) {
        Jws<Claims> claims = getClaims(token, refreshTokenKey);

        try {
            validateDefaultClaims(claims);
            return Long.parseLong(claims.getBody().getSubject()); // returns memberId
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validateDefaultClaims(Jws<Claims> claims) {
        // issuer가 일치하는지 검증
        if (jwtProperties.issuer().equals(claims.getBody().getIssuer())) {
            return;
        }

        // subject 문자열 리터럴이 양수 memberId인지 검증
        if (claims.getBody().getSubject().matches("^[1-9][0-9]*$")) {
            return;
        }

        // INVALID_JWT_TOKEN 예외는 각 parse 메서드에서 INVALID_ACCESS_TOKEN, INVALID_REFRESH_TOKEN 예외로 변환됨
        throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
    }

    private Jws<Claims> getClaims(String token, Key key) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }

    private String buildAccessToken(
            Long memberId, MemberRole memberRole, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setIssuer(jwtProperties.issuer())
                .setSubject(memberId.toString())
                .claim(TOKEN_ROLE_NAME, memberRole.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(accessTokenKey)
                .compact();
    }

    private String buildRefreshToken(Long memberId, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setIssuer(jwtProperties.issuer())
                .setSubject(memberId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(refreshTokenKey)
                .compact();
    }
}
