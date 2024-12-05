package com.depromeet.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.global.security.PrincipalDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MemberUtilTest {

    @Autowired private MemberUtil memberUtil;
    @Autowired private MemberRepository memberRepository;

    @Test
    void 현재_로그인한_회원의_정보를_정상적으로_반환한다() {
        // given
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Member member =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test", "test", "test"), "test");
        Member savedMember = memberRepository.save(member);
        // when
        Member currentMember = memberUtil.getCurrentMember();
        // then
        assertEquals(savedMember.getId(), currentMember.getId());
    }
}
