package com.depromeet.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.depromeet.domain.member.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberUtilTest {

	@Autowired
	private MemberUtil memberUtil;

	@Test
	void 현재_로그인한_회원ID는_1이다() {
		// given & when
		Member currentMember = memberUtil.getCurrentMember();

		// then
		assertEquals(1L, currentMember.getId());
	}
}
