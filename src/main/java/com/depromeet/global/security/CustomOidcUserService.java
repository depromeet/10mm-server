package com.depromeet.global.security;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;

    public CustomOidcUserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        setAccessibleScopes(Collections.emptySet()); // 빈 스코프로 설정하여 항상 UserInfo 엔드포인트에 액세스
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);
        Member member = fetchOrCreate(oidcUser);

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
