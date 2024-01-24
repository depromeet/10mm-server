package com.depromeet.domain.member.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberTest {

    // Fixture
    Profile profile;

    @BeforeEach
    void setUp() {
        profile = Profile.createProfile("testNickname", "testProfileImageUrl");
    }

    @Test
    void 회원가입시_초기_상태는_NORMAL이다() {
        // given
        Member member =
                Member.createGuestMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberStatus status = member.getStatus();

        // then
        assertEquals(MemberStatus.NORMAL, status);
    }

    @Test
    void 회원가입시_초기_역할은_GUEST이다() {
        // given
        Member member =
                Member.createGuestMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberRole role = member.getRole();

        // then
        assertEquals(MemberRole.GUEST, role);
    }

    @Test
    void 회원가입시_초기_공개여부는_PUBLIC이다() {
        // given
        Member member =
                Member.createGuestMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        MemberVisibility visibility = member.getVisibility();

        // then
        assertEquals(MemberVisibility.PUBLIC, visibility);
    }

    @Test
    void 회원가입시_게스트멤버의_닉네임이_설정된다() {
        // given
        Member member =
                Member.createGuestMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        member.register("testNickname");

        // then
        assertEquals("testNickname", member.getProfile().getNickname());
    }

    @Test
    void 회원가입시_게스트멤버는_일반멤버로_변경된다() {
        // given
        Member member =
                Member.createGuestMember(
                        OauthInfo.createOauthInfo("testProvider", "testProviderId", "testEmail"),
                        "testNickname");

        // when
        member.register("testNickname");

        // then
        assertEquals(MemberRole.USER, member.getRole());
    }

    @Test
    void 회원가입시_일반멤버이면_예외가_발생한다() {
        // given
        Member member = Member.createNormalMember(profile);

        // when & then
        assertThatThrownBy(() -> member.register("testNickname"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_ALREADY_REGISTERED.getMessage());
    }

    @Test
    void 이미_프로필사진이_존재할때_회원가입해도_프로필사진이_변경되지_않는다() {}
}
