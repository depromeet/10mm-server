package com.depromeet.domain.auth.application;

import com.depromeet.domain.auth.dto.request.MemberRegisterRequest;
import com.depromeet.domain.auth.dto.request.UsernamePasswordRequest;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.global.util.MemberUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;

    public void registerMember(MemberRegisterRequest request) {
        final Member member = memberUtil.getCurrentMember();
        member.register(request.nickname());
    }

    public void registerWithUsernameAndPassword(UsernamePasswordRequest request) {
        final Member member = Member.createGuestMember(request.username(), request.password());
        memberRepository.save(member);
    }
}
