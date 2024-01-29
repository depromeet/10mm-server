package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import com.depromeet.domain.auth.application.JwtTokenService;
import com.depromeet.domain.auth.dto.response.AccessToken;
import com.depromeet.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import org.springframework.web.util.WebUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // TODO: 쿠키 방식으로 변경 시 아래 로직 수정 필요
        String accessToken = extractAccessTokenFromCookie(request);
        String refreshToken = extractRefreshTokenFromCookie(request);

        // TODO: 엑세스 토큰 없더라도 쿠키의 리프레시 토큰으로 재발급하도록 수정 필요
        // ATK, RTK 둘 중 하나라도 빈 상태로 오면 통과
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
            refreshToken = jwtTokenService.reissueRefreshToken(refreshToken);
        }

        // ATK 만료되지 않았고 RTK 만료되었으면 ATK로 ATK, RTK 재발급
        if (!isAccessTokenExpired && isRefreshTokenExpired) {
            AccessToken accessTokenDto = jwtTokenService.parseAccessToken(accessToken);
            accessToken = jwtTokenService.reissueAccessToken(accessTokenDto);
            refreshToken = jwtTokenService.reissueRefreshToken(accessTokenDto);
        }

        // ATK, RTK 둘 다 만료되지 않았으면 RTK 재발급
        if (!isAccessTokenExpired && !isRefreshTokenExpired) {
            AccessToken accessTokenDto = jwtTokenService.parseAccessToken(accessToken);
            refreshToken = jwtTokenService.reissueRefreshToken(accessTokenDto);
        }

        cookieUtil.addTokenCookies(response, accessToken, refreshToken);
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

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .orElse(null);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .orElse(null);
    }
}
