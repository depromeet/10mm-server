package com.depromeet.domain.member.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberTest {

	// Fixture
	Profile profile;

	@BeforeEach
	void setUp() {
		profile = new Profile("testNickname", "testProfileImageUrl");
	}

	@Test
	void 회원가입시_초기_상태는_NORMAL이다() {
		// given
		Member member = Member.createNormalMember(profile);

		// when
		MemberStatus status = member.getStatus();

		// then
		assertEquals(MemberStatus.NORMAL, status);
	}

	@Test
	void 회원가입시_초기_역할은_USER이다() {
		// given
		Member member = Member.createNormalMember(profile);

		// when
		MemberRole role = member.getRole();

		// then
		assertEquals(MemberRole.USER, role);
	}

	@Test
	void 회원가입시_초기_공개여부는_PUBLIC이다() {
		// given
		Member member = Member.createNormalMember(profile);

		// when
		MemberVisibility visibility = member.getVisibility();

		// then
		assertEquals(MemberVisibility.PUBLIC, visibility);
	}
}
