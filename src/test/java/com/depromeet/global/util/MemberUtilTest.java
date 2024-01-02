package com.depromeet.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberUtilTest {

    @Autowired private MemberUtil memberUtil;
    @Autowired private MemberRepository memberRepository;
    @Autowired private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @Test
    void 이미_회원이_존재하면_임시_회원을_삽입하지_않는다() {
        // given
        Member member = Member.createNormalMember(new Profile("testNickname", "testImageUrl"));
        memberRepository.save(member);

        // when
        memberUtil.getCurrentMember();

        // then
        assertEquals(1, memberRepository.count());
    }

    @Test
    void 현재_로그인한_회원ID는_1이다() {
        // given & when
        Member currentMember = memberUtil.getCurrentMember();

        // then
        assertEquals(1L, currentMember.getId());
    }
}
