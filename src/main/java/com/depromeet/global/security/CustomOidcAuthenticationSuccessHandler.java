package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.TOKEN_PREFIX;

import com.depromeet.domain.member.dao.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOidcAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        log.info("authentication success");

        CustomOidcUser user = (CustomOidcUser) authentication.getPrincipal();

		// 게스트인 경우 회원가입 페이지로 리다이렉트
        if (user.isGuest()) {
            response.sendRedirect("/auth/signup");
        }

        String accessToken =
                jwtTokenProvider.generateAccessToken(user.getMemberId(), user.getMemberRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getMemberId());

        setTokenPairToResponseHeader(response, accessToken, refreshToken);
    }

    private void setTokenPairToResponseHeader(
            HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader("Authorization", TOKEN_PREFIX + accessToken);
        response.setHeader("RefreshToken", TOKEN_PREFIX + refreshToken);
    }
}
