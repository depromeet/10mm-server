package com.depromeet.domain.auth.api;

import com.depromeet.domain.auth.application.AuthService;
import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.TokenPairResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "토큰 발급을 위해 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> memberLogin(
            @Valid @RequestBody UsernamePasswordRequest request) {
        TokenPairResponse response = authService.loginMember(request);
        return ResponseEntity.ok(response);
    }
}
