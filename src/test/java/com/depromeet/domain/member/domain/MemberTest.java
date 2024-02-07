package com.depromeet.domain.member.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 소셜_로그인시_초기_상태는_NORMAL이다() {
        // given
        Member member =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberStatus status = member.getStatus();

        // then
        assertEquals(MemberStatus.NORMAL, status);
    }

    @Test
    void 소셜_로그인시_초기_역할은_USER이다() {
        // given
        Member member =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberRole role = member.getRole();

        // then
        assertEquals(MemberRole.USER, role);
    }

    @Test
    void 소셜_로그인시시_초기_계정공개여부는_PUBLIC이다() {
        // given
        Member member =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberVisibility visibility = member.getVisibility();

        // then
        assertEquals(MemberVisibility.PUBLIC, visibility);
    }

    @Test
    void 이미_프로필사진이_존재할때_회원가입해도_프로필사진이_변경되지_않는다() {}
}
