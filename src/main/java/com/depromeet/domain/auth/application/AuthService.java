package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.LoginResponse;
import com.depromeet.domain.auth.dto.response.MemberTempRegisterResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public void registerMember(MemberRegisterRequest request) {
        final Member member = memberUtil.getCurrentMember();
        member.register(request.nickname());
    }

    public MemberTempRegisterResponse registerWithUsernameAndPassword(
            UsernamePasswordRequest request) {
        validateUniqueUsername(request.username());

        String encodedPassword = passwordEncoder.encode(request.password());
        final Member member = Member.createGuestMember(request.username(), encodedPassword);

        Member savedMember = memberRepository.save(member);
        return MemberTempRegisterResponse.from(savedMember.getId());
    }

    private void validateUniqueUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }

    public LoginResponse loginMember(UsernamePasswordRequest request) {
        final Member member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateNotGuestMember(member);
        validatePasswordMatches(member, request.password());

        String accessToken = jwtTokenService.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        return LoginResponse.from(accessToken, refreshToken);
    }

    private void validateNotGuestMember(Member member) {
        if (member.isGuest()) {
            throw new CustomException(ErrorCode.GUEST_MEMBER_REQUIRES_REGISTRATION);
        }
    }

    private void validatePasswordMatches(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHES);
        }
    }
}
