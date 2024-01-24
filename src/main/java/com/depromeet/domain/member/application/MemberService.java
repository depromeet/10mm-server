package com.depromeet.domain.member.application;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.domain.member.dto.response.MemberFindOneResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public MemberFindOneResponse findMemberInfo() {
        final Member currentMember = memberUtil.getCurrentMember();
        return MemberFindOneResponse.from(currentMember);
    }

    @Transactional(readOnly = true)
    public void checkUsername(UsernameCheckRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request) {
        if (memberRepository.existsByProfileNickname(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_NICKNAME);
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

    public MemberSocialInfoResponse findMemberSocialInfo() {
        final Member currentMember = memberUtil.getCurrentMember();
        validateSocialInfoNotNull(currentMember);
        return MemberSocialInfoResponse.from(currentMember);
    }

    private void validateSocialInfoNotNull(Member member) {
        if (member.getOauthInfo() == null) {
            throw new CustomException(ErrorCode.MEMBER_SOCIAL_INFO_NOT_FOUND);
        }
    }
}
