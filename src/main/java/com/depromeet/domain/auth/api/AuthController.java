package com.depromeet.domain.auth.api;

import com.depromeet.domain.auth.application.AuthService;
import com.depromeet.domain.auth.application.JwtTokenService;
import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @PostMapping("/register")
    public ResponseEntity<Void> memberRegister(@Valid @RequestBody MemberRegisterRequest request) {
        authService.registerMember(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "아이디/비밀번호 임시 회원가입", description = "아이디/비밀번호 임시 회원가입을 진행합니다.")
    @PostMapping("/register/temp")
    public ResponseEntity<Void> memberTempRegister(
            @Valid @RequestBody UsernamePasswordRequest request) {
        authService.registerWithUsernameAndPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<Void> memberLogin(@Valid @RequestBody UsernamePasswordRequest request) {
        return ResponseEntity.ok().build();
    }
}
