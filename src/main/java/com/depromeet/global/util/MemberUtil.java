package com.depromeet.global.util;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;

	// TODO: 데이터베이스 연동 및 픽스처 데이터 삽입 이후 삭제
    private void insertMockMemberIfNotExist() {
        if (memberRepository.count() != 0) {
            return;
        }

        Member memeber = Member.createNormalMember(new Profile("testNickname", "testImageUrl"));

        memberRepository.save(memeber);
    }

    public Member getCurrentMember() {
        insertMockMemberIfNotExist();

        return memberRepository
                .findById(securityUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
