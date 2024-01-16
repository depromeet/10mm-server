package com.depromeet.domain.member.application;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public void checkUsername(UsernameCheckRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }

    public void withdrawal(UsernameCheckRequest request) {
        final Member member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteById(member.getId());
        member.withdrawal();
    }
}