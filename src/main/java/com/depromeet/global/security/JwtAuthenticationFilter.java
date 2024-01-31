package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import com.depromeet.domain.auth.application.JwtTokenService;
import com.depromeet.domain.auth.dto.AccessTokenDto;
import com.depromeet.domain.auth.dto.RefreshTokenDto;
import com.depromeet.domain.member.domain.MemberRole;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CookieUtil cookieUtil;

    private static String extractAccessTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessTokenHeaderValue = extractAccessTokenFromHeader(request);
        String accessTokenValue = extractAccessTokenFromCookie(request);
        String refreshTokenValue = extractRefreshTokenFromCookie(request);

        // 헤더에 AT가 있으면 우선적으로 검증
        if (accessTokenHeaderValue != null) {
            AccessTokenDto accessTokenDto =
                    jwtTokenService.retrieveAccessToken(accessTokenHeaderValue);
            if (accessTokenDto != null) {
                setAuthenticationToContext(accessTokenDto.memberId(), accessTokenDto.memberRole());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 쿠키에서 가져올 때 AT와 RT 중 하나라도 없으면 실패
        if (accessTokenValue == null || refreshTokenValue == null) {
            filterChain.doFilter(request, response);
            return;
        }

        AccessTokenDto accessTokenDto = jwtTokenService.retrieveAccessToken(accessTokenValue);

        // AT가 유효하면 통과
        if (accessTokenDto != null) {
            setAuthenticationToContext(accessTokenDto.memberId(), accessTokenDto.memberRole());
            filterChain.doFilter(request, response);
            return;
        }

        // AT가 만료된 경우 AT 재발급, 만료되지 않은 경우 null 반환
        Optional<AccessTokenDto> reissuedAccessToken =
                Optional.ofNullable(jwtTokenService.reissueAccessTokenIfExpired(accessTokenValue));
        // RT 유효하면 파싱, 유효하지 않으면 null 반환
        RefreshTokenDto refreshTokenDto = jwtTokenService.retrieveRefreshToken(refreshTokenValue);

        // AT가 만료되었고, RT가 유효하면 AT, RT 재발급
        if (reissuedAccessToken.isPresent() && refreshTokenDto != null) {
            AccessTokenDto accessToken = reissuedAccessToken.get(); // 재발급된 AT
            RefreshTokenDto refreshToken =
                    jwtTokenService.createRefreshTokenDto(refreshTokenDto.memberId());

            // 쿠키에 재발급된 AT, RT 저장
            HttpHeaders httpHeaders =
                    cookieUtil.generateTokenCookies(
                            accessToken.tokenValue(), refreshToken.tokenValue());
            response.addHeader(
                    HttpHeaders.SET_COOKIE, httpHeaders.getFirst(ACCESS_TOKEN_COOKIE_NAME));
            response.addHeader(
                    HttpHeaders.SET_COOKIE, httpHeaders.getFirst(REFRESH_TOKEN_COOKIE_NAME));

            setAuthenticationToContext(accessToken.memberId(), accessToken.memberRole());
        }

        // AT, RT가 모두 만료된 경우 실패
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationToContext(Long memberId, MemberRole memberRole) {
        UserDetails userDetails = new PrincipalDetails(memberId, memberRole.toString());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
