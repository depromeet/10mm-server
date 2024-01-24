package com.depromeet.domain.member.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import com.depromeet.global.util.MemberUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser
class MemberServiceTest {

    @Autowired EntityManager em;
    @Autowired DatabaseCleaner databaseCleaner;
    @Autowired MemberUtil memberUtil;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    private void saveAndRegisterMember(OauthInfo oauthInfo) {
        Member member = Member.createGuestMember(oauthInfo, "testNickname");
        memberRepository.save(member);
        member.register("testNickname");
        PrincipalDetails principalDetails = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    class 소셜_로그인_정보를_조회할때 {
        @Test
        void 성공한다() {
            // given
            OauthInfo oauthInfo =
                    OauthInfo.createOauthInfo(
                            "testOauthId", OauthProvider.KAKAO.getIssuer(), "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when
            MemberSocialInfoResponse response = memberService.findMemberSocialInfo();

            // then
            assertEquals(OauthProvider.KAKAO, response.provider());
            assertEquals("testEmail", response.email());
        }

        @Test
        void 멤버의_프로바이더_정보와_일치하는_issuer가_없으면_예외가_발생한다() {
            // given
            String invalidIssuer = "invalidIssuer";
            OauthInfo oauthInfo =
                    OauthInfo.createOauthInfo("testOauthId", invalidIssuer, "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.OAUTH_PROVIDER_NOT_FOUND.getMessage());
        }

        @Test
        void 멤버의_프로바이더가_null이면_예외가_발생한다() {
            // given
            OauthInfo oauthInfo = OauthInfo.createOauthInfo("testOauthId", null, "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.OAUTH_PROVIDER_NOT_FOUND.getMessage());
        }

        @Test
        void 멤버의_OauthInfo가_비어있으면_예외가_발생한다() {
            // given
            saveAndRegisterMember(null);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.MEMBER_SOCIAL_INFO_NOT_FOUND.getMessage());
        }
    }
}
