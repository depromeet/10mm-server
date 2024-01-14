package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.auth.dto.response.MemberTempRegisterResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;

    public void registerMember(MemberRegisterRequest request) {
        final Member member = memberUtil.getCurrentMember();
        member.register(request.nickname());
    }

    public MemberTempRegisterResponse registerWithUsernameAndPassword(
            UsernamePasswordRequest request) {
        validateUniqueUsername(request.username());

        final Member member = Member.createGuestMember(request.username(), request.password());
        Member savedMember = memberRepository.save(member);
        return MemberTempRegisterResponse.from(savedMember.getId());
    }

    private void validateUniqueUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }
}
