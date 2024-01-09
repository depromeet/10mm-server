package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.domain.RefreshToken;
import com.depromeet.domain.auth.dto.AccessToken;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.global.config.security.PrincipalDetails;
import com.depromeet.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Long memberId, MemberRole memberRole) {
        return jwtTokenProvider.generateAccessToken(memberId, memberRole);
    }

    public String createRefreshToken(Long memberId) {
        String token = jwtTokenProvider.generateRefreshToken(memberId);
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .ttl(jwtTokenProvider.getRefreshTokenExpirationTime())
                        .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public Authentication getAuthentication(String accessToken) {
        AccessToken parsedAccessToken = jwtTokenProvider.parseAccessToken(accessToken);

        UserDetails userDetails =
                new PrincipalDetails(
                        parsedAccessToken.memberId(), parsedAccessToken.memberRole().name());

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
