package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import com.depromeet.domain.auth.application.JwtTokenService;
import com.depromeet.domain.auth.dto.AccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        // ATK, RTK 둘 다 없으면 통과
        if (accessToken == null || refreshToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAccessTokenExpired = jwtTokenService.isAccessTokenExpired(accessToken);
        boolean isRefreshTokenExpired = jwtTokenService.isRefreshTokenExpired(refreshToken);

        // ATK, RTK 둘 다 만료되었으면 통과
        if (isAccessTokenExpired && isRefreshTokenExpired) {
            filterChain.doFilter(request, response);
            return;
        }

        // ATK 만료되었고 RTK 만료되지 않았으면 RTK로 ATK, RTK 재발급
        if (isAccessTokenExpired && !isRefreshTokenExpired) {
            accessToken = jwtTokenService.reissueAccessToken(refreshToken);
            jwtTokenService.reissueRefreshToken(refreshToken);
        }

        // ATK 만료되지 않았고 RTK 만료되었으면 ATK로 ATK, RTK 재발급
        if (!isAccessTokenExpired && isRefreshTokenExpired) {
            AccessToken accessTokenDto = jwtTokenService.parseAccessToken(accessToken);
            accessToken = jwtTokenService.reissueAccessToken(accessTokenDto);
            jwtTokenService.reissueRefreshToken(accessTokenDto);
        }

        // ATK, RTK 둘 다 만료되지 않았으면 RTK 재발급
        if (!isAccessTokenExpired && !isRefreshTokenExpired) {
            AccessToken accessTokenDto = jwtTokenService.parseAccessToken(accessToken);
            jwtTokenService.reissueRefreshToken(accessTokenDto);
        }

        Authentication authentication = jwtTokenService.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(REFRESH_TOKEN_HEADER))
                .filter(token -> token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }

    private String extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_TOKEN_HEADER))
                .filter(token -> token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }
}
