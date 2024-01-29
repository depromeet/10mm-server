package com.depromeet.domain.auth.application;

import static com.depromeet.global.common.constants.SecurityConstants.TOKEN_ROLE_NAME;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.domain.RefreshToken;
import com.depromeet.domain.auth.dto.AccessTokenDto;
import com.depromeet.domain.auth.dto.RefreshTokenDto;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.global.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessToken(memberId, memberRole);
    }

    public AccessTokenDto createAccessTokenDto(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessTokenDto(memberId, memberRole);
    }

    public String createRefreshToken(Long memberId) {
        String token = jwtUtil.generateRefreshToken(memberId);
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .ttl(jwtUtil.getRefreshTokenExpirationTime())
                        .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public RefreshTokenDto createRefreshTokenDto(Long memberId) {
        RefreshTokenDto refreshTokenDto = jwtUtil.generateRefreshTokenDto(memberId);
        saveRefreshTokenToRedis(memberId, refreshTokenDto.tokenValue(), refreshTokenDto.ttl());
        return refreshTokenDto;
    }

    private void saveRefreshTokenToRedis(
            Long memberId, String refreshTokenDto, Long refreshTokenDto1) {
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(refreshTokenDto)
                        .ttl(refreshTokenDto1)
                        .build();
        refreshTokenRepository.save(refreshToken);
    }

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            return jwtUtil.parseAccessToken(accessTokenValue);
        } catch (Exception e) {
            return null;
        }
    }

    public RefreshTokenDto retrieveRefreshToken(String refreshTokenValue) {
        RefreshTokenDto refreshTokenDto = parseRefreshToken(refreshTokenValue);

        if (refreshTokenDto == null) {
            return null;
        }

        // 파싱된 DTO와 일치하는 토큰이 Redis에 저장되어 있는지 확인
        Optional<RefreshToken> refreshToken = getRefreshTokenFromRedis(refreshTokenDto.memberId());

        // Redis에 토큰이 존재하고, 쿠키의 토큰과 값이 일치하면 DTO 반환
        if (refreshToken.isPresent()
                && refreshTokenDto.tokenValue().equals(refreshToken.get().getToken())) {
            return refreshTokenDto;
        }

        // Redis에 토큰이 존재하지 않거나, 쿠키의 토큰과 값이 일치하지 않으면 null 반환
        return null;
    }

    private Optional<RefreshToken> getRefreshTokenFromRedis(Long memberId) {
        return refreshTokenRepository.findById(memberId);
    }

    private RefreshTokenDto parseRefreshToken(String refreshTokenValue) {
        try {
            return jwtUtil.parseRefreshToken(refreshTokenValue);
        } catch (Exception e) {
            return null;
        }
    }

    public AccessTokenDto reissueAccessTokenIfExpired(String accessTokenValue) {
        // AT가 만료된 경우 AT 재발급, 만료되지 않은 경우 null 반환
        try {
            jwtUtil.parseAccessToken(accessTokenValue);
            return null;
        } catch (ExpiredJwtException e) {
            Long memberId = Long.parseLong(e.getClaims().getSubject());
            MemberRole memberRole =
                    MemberRole.valueOf(e.getClaims().get(TOKEN_ROLE_NAME, String.class));
            return createAccessTokenDto(memberId, memberRole);
        }
    }
}
