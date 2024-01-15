package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TokenPairResponse registerWithUsernameAndPassword(UsernamePasswordRequest request) {
        validateUniqueUsername(request.username());

        String encodedPassword = passwordEncoder.encode(request.password());
        final Member member = Member.createGuestMember(request.username(), encodedPassword);

        Member savedMember = memberRepository.save(member);
        return getLoginResponse(savedMember);
    }

    private void validateUniqueUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }

    public TokenPairResponse loginMember(UsernamePasswordRequest request) {
        final Member member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateNotGuestMember(member);
        validatePasswordMatches(member, request.password());

        return getLoginResponse(member);
    }

    private void validateNotGuestMember(Member member) {
        if (member.getRole() == MemberRole.GUEST) {
            throw new CustomException(ErrorCode.GUEST_MEMBER_REQUIRES_REGISTRATION);
        }
    }

    private void validatePasswordMatches(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHES);
        }
    }

    private TokenPairResponse getLoginResponse(Member member) {
        String accessToken = jwtTokenService.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        return TokenPairResponse.from(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public void checkUsername(UsernameCheckRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }
}
