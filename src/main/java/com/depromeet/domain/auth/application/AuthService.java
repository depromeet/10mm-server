package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.application.nickname.NicknameGenerationStrategy;
import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.auth.dto.request.IdTokenRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.SocialLoginResponse;
import com.depromeet.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberStatus;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final IdTokenVerifier idTokenVerifier;
    private final NicknameGenerationStrategy nicknameGenerationStrategy;

    @Deprecated
    public TokenPairResponse registerWithUsernameAndPassword(UsernamePasswordRequest request) {
        Optional<Member> member = memberRepository.findByUsername(request.username());

        // 첫 회원가입
        if (member.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.password());
            final Member savedMember = Member.createNormalMember(null, null); // do nothing
            memberRepository.save(savedMember);
            return getLoginResponse(savedMember);
        }

        // 토큰 만료된, 이미 임시 회원가입한 게스트 회원
        Member existMember = member.get();
        validatePasswordMatches(existMember, request.password());
        return getLoginResponse(existMember);
    }

    public TokenPairResponse loginMember(UsernamePasswordRequest request) {
        final Member member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateNotGuestMember(member);
        validatePasswordMatches(member, request.password());
        validateNormalMember(member);

        member.updateLastLoginAt();

        return getLoginResponse(member);
    }

    @Deprecated
    private void validateNotGuestMember(Member member) {
        // do nothing
    }

    private void validateNormalMember(Member member) {
        if (member.getStatus() != MemberStatus.NORMAL) {
            throw new CustomException(ErrorCode.MEMBER_INVALID_NORMAL);
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

    public SocialLoginResponse socialLoginMember(IdTokenRequest request, OauthProvider provider) {
        OidcUser oidcUser = idTokenVerifier.getOidcUser(request.idToken(), provider);
        Member member = fetchOrCreate(oidcUser);
        member.updateLastLoginAt();

        TokenPairResponse loginResponse = getLoginResponse(member);

        return SocialLoginResponse.from(loginResponse);
    }

    private Member fetchOrCreate(OidcUser oidcUser) {
        return memberRepository
                .findByOauthInfo(extractOauthInfo(oidcUser))
                .orElseGet(() -> saveAsGuest(oidcUser));
    }

    private Member saveAsGuest(OidcUser oidcUser) {

        OauthInfo oauthInfo = extractOauthInfo(oidcUser);
        String nickname = generateRandomNickname();
        Member guest = Member.createNormalMember(oauthInfo, nickname);
        return memberRepository.save(guest);
    }

    private String generateRandomNickname() {
        while (true) {
            String nickname = nicknameGenerationStrategy.generate();
            if (!memberRepository.existsByProfileNickname(nickname)) {
                return nickname;
            }
        }
    }

    private OauthInfo extractOauthInfo(OidcUser oidcUser) {
        return OauthInfo.createOauthInfo(
                oidcUser.getName(), oidcUser.getIssuer().toString(), oidcUser.getEmail());
    }
}
