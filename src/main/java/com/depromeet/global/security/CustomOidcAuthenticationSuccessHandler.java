package com.depromeet.global.security;

import static com.depromeet.global.common.constants.SecurityConstants.*;

import com.depromeet.domain.auth.application.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOidcAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        log.info("authentication success");

        CustomOidcUser user = (CustomOidcUser) authentication.getPrincipal();

        if (user.isGuest()) {
            response.sendRedirect("/register");
        }

        String accessToken =
                jwtTokenService.createAccessToken(user.getMemberId(), user.getMemberRole());
        String refreshToken = jwtTokenService.createRefreshToken(user.getMemberId());

        setTokenPairToResponseHeader(response, accessToken, refreshToken);
    }

    private void setTokenPairToResponseHeader(
            HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + accessToken);
        response.setHeader(REFRESH_TOKEN_HEADER, TOKEN_PREFIX + refreshToken);
    }
}
