package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.auth.dto.request.IdTokenRequest;
import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.SocialLoginResponse;
import com.depromeet.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberRole;
import com.depromeet.domain.member.domain.MemberStatus;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.CustomOidcUser;
import com.depromeet.global.util.MemberUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    private final IdTokenVerifier idTokenVerifier;

    public void registerMember(MemberRegisterRequest request) {
        final Member member = memberUtil.getCurrentMember();
        member.register(request.nickname());
    }

    public TokenPairResponse registerWithUsernameAndPassword(UsernamePasswordRequest request) {
        Optional<Member> member = memberRepository.findByUsername(request.username());

        // 첫 회원가입
        if (member.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.password());
            final Member savedMember =
                    Member.createGuestMember(request.username(), encodedPassword);
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

    private void validateNotGuestMember(Member member) {
        if (member.getRole() == MemberRole.GUEST) {
            throw new CustomException(ErrorCode.GUEST_MEMBER_REQUIRES_REGISTRATION);
        }
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
        OidcUser oidcUser = getOidcUser(request.idToken(), provider);
        Member member = fetchOrCreate(oidcUser);
        TokenPairResponse loginResponse = getLoginResponse(member);
        boolean isGuest = member.getRole() == MemberRole.GUEST;
        return SocialLoginResponse.from(loginResponse, isGuest);
    }

    private OidcUser getOidcUser(String idToken, OauthProvider provider) {
        OidcUser oidcUser = idTokenVerifier.getOidcUser(idToken, provider);
        Member member = fetchOrCreate(oidcUser);
        member.updateLastLoginAt();
        return new CustomOidcUser(oidcUser, member.getId(), member.getRole());
    }

    private Member fetchOrCreate(OidcUser oidcUser) {
        return memberRepository
                .findByOauthInfo(extractOauthInfo(oidcUser))
                .orElseGet(() -> saveAsGuest(oidcUser));
    }

    private Member saveAsGuest(OidcUser oidcUser) {
        OauthInfo oauthInfo = extractOauthInfo(oidcUser);
        Member guest = Member.createGuestMember(oauthInfo);
        return memberRepository.save(guest);
    }

    private OauthInfo extractOauthInfo(OidcUser oidcUser) {
        return OauthInfo.createOauthInfo(oidcUser.getName(), oidcUser.getIssuer().toString());
    }
}
