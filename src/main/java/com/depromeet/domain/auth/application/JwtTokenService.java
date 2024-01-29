package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.domain.RefreshToken;
import com.depromeet.domain.auth.dto.AccessTokenDto;
import com.depromeet.domain.auth.dto.RefreshTokenDto;
import com.depromeet.domain.auth.dto.response.AccessToken;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.global.security.JwtTokenProvider;
import com.depromeet.global.security.PrincipalDetails;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Long memberId, MemberRole memberRole) {
        return jwtTokenProvider.generateAccessToken(memberId, memberRole);
    }

    public AccessTokenDto createAccessTokenDto(Long memberId, MemberRole memberRole) {
        return jwtTokenProvider.generateAccessTokenDto(memberId, memberRole);
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

    public RefreshTokenDto createRefreshTokenDto(Long memberId) {
        RefreshTokenDto refreshTokenDto = jwtTokenProvider.generateRefreshTokenDto(memberId);
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

    public String reissueAccessToken(String refreshToken) {
        Member member = getMemberFrom(refreshToken);
        return createAccessToken(member.getId(), member.getRole());
    }

    public String reissueAccessToken(AccessToken accessToken) {
        return createAccessToken(accessToken.memberId(), accessToken.memberRole());
    }

    public String reissueRefreshToken(String refreshToken) {
        Member member = getMemberFrom(refreshToken);
        return createRefreshToken(member.getId());
    }

    public String reissueRefreshToken(AccessToken accessToken) {
        return createRefreshToken(accessToken.memberId());
    }

    private Member getMemberFrom(String refreshToken) throws NoSuchElementException {
        Long memberId = jwtTokenProvider.parseRefreshToken(refreshToken);
        return memberRepository.findById(memberId).orElseThrow();
    }

    public Authentication getAuthentication(String accessToken) {
        AccessToken parsedAccessToken = jwtTokenProvider.parseAccessToken(accessToken);

        UserDetails userDetails =
                new PrincipalDetails(
                        parsedAccessToken.memberId(), parsedAccessToken.memberRole().name());

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    public boolean isAccessTokenExpired(String accessToken) {
        return jwtTokenProvider.isAccessTokenExpired(accessToken);
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        return jwtTokenProvider.isRefreshTokenExpired(refreshToken);
    }

    public AccessToken parseAccessToken(String accessToken) {
        return jwtTokenProvider.parseAccessToken(accessToken);
    }
}
