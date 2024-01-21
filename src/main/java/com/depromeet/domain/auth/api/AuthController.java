package com.depromeet.domain.auth.api;

import com.depromeet.domain.auth.application.AuthService;
import com.depromeet.domain.auth.application.IdTokenVerifier;
import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.auth.dto.request.IdTokenRequest;
import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.SocialLoginResponse;
import com.depromeet.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.global.util.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1-1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final IdTokenVerifier idTokenVerifier;

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @PostMapping("/register")
    public ResponseEntity<Void> memberRegister(@Valid @RequestBody MemberRegisterRequest request) {
        authService.registerMember(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "아이디/비밀번호 임시 회원가입", description = "아이디/비밀번호 임시 회원가입을 진행합니다.")
    @PostMapping("/temp-register")
    public ResponseEntity<TokenPairResponse> memberTempRegister(
            @Valid @RequestBody UsernamePasswordRequest request) {
        TokenPairResponse response = authService.registerWithUsernameAndPassword(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).headers(tokenHeaders).body(response);
    }

    @Operation(summary = "로그인", description = "토큰 발급을 위해 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> memberLogin(
            @Valid @RequestBody UsernamePasswordRequest request) {
        TokenPairResponse response = authService.loginMember(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.ok().headers(tokenHeaders).body(response);
    }

    @Operation(
            summary = "소셜 로그인",
            description = "소셜 로그인 후 토큰을 발급합니다. 가입하지 않은 유저인 경우 임시 회원가입을 진행합니다.")
    @PostMapping("/social-login")
    public ResponseEntity<SocialLoginResponse> memberSocialLogin(
            @RequestParam OauthProvider provider, @Valid @RequestBody IdTokenRequest request) {

        SocialLoginResponse response = authService.socialLoginMember(request, provider);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.ok().headers(tokenHeaders).body(response);
    }
}
