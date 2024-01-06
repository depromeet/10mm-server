package com.depromeet.global.security;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Set<String> KAKAO_ACCESSIBLE_SCOPES = Set.of("account_email");
    private final MemberRepository memberRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        Member member = fetchOrCreate(userRequest);
        setAccessibleScopes(KAKAO_ACCESSIBLE_SCOPES);
        OidcUser oidcUser = super.loadUser(userRequest);
        return oidcUser;
    }

    private Member fetchOrCreate(OidcUserRequest request) {
        return memberRepository
                .findByOauthInfo(extractOauthInfo(request))
                .orElseGet(() -> saveAsGuest(request));
    }

    private Member saveAsGuest(OidcUserRequest request) {
        OauthInfo oauthInfo = extractOauthInfo(request);
        Member member = Member.createGuestMember(oauthInfo);
        return memberRepository.save(member);
    }

    private OauthInfo extractOauthInfo(OidcUserRequest request) {
        return OauthInfo.builder()
                .oauthId(request.getIdToken().getSubject())
                .oauthProvider(request.getIdToken().getIssuer().toString())
                .build();
    }

    private OidcUserInfo extractOidcUserInfo(Member member) {
        return OidcUserInfo.builder().name(member.getId().toString()).build();
    }
}
