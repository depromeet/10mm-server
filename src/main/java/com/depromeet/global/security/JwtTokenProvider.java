package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.TOKEN_ROLE_NAME;

import com.depromeet.domain.auth.dto.response.AccessToken;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.infra.config.jwt.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

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
        Jws<Claims> claims = getClaims(token, getAccessTokenKey());

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
        Jws<Claims> claims = getClaims(token, getRefreshTokenKey());

        try {
            validateDefaultClaims(claims);
            return Long.parseLong(claims.getBody().getSubject()); // returns memberId
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    // TODO: ATK와 RTK의 유효성 검증을 한 번에 할 수 있도록 리팩토링
    // TODO: 인증 사이클 당 2회의 토큰 파싱이 발생하는 이슈 개선
    public boolean isAccessTokenExpired(String token) {
        try {
            parseAccessToken(token);
            return false;
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.EXPIRED_JWT_TOKEN) {
                return true;
            }
            throw e;
        }
    }

    public boolean isRefreshTokenExpired(String token) {
        try {
            parseRefreshToken(token);
            return false;
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.EXPIRED_JWT_TOKEN) {
                return true;
            }
            throw e;
        }
    }

    public long getRefreshTokenExpirationTime() {
        return jwtProperties.refreshTokenExpirationTime();
    }

    private Key getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.refreshTokenSecret().getBytes());
    }

    private Key getAccessTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.accessTokenSecret().getBytes());
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
        return Jwts.parserBuilder()
                .requireIssuer(jwtProperties.issuer())
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    private String buildAccessToken(
            Long memberId, MemberRole memberRole, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setIssuer(jwtProperties.issuer())
                .setSubject(memberId.toString())
                .claim(TOKEN_ROLE_NAME, memberRole.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getAccessTokenKey())
                .compact();
    }

    private String buildRefreshToken(Long memberId, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setIssuer(jwtProperties.issuer())
                .setSubject(memberId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getRefreshTokenKey())
                .compact();
    }
}
