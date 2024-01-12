package com.depromeet.global.security;

import com.depromeet.global.error.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
public class CustomOidcAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        log.error("Authentication failed: {}", exception.getMessage());
        ErrorCode errorCode = ErrorCode.SOCIAL_AUTHENTICATION_FAILED;
        response.sendError(errorCode.getStatus().value(), errorCode.getMessage());
    }
}
