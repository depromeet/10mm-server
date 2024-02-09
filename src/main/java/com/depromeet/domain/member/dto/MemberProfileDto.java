package com.depromeet.domain.member.dto;

import com.depromeet.domain.member.domain.Member;

public record MemberProfileDto(Long memberId, String nickname, String profileImageUrl) {

    public static MemberProfileDto from(Member member) {
        return new MemberProfileDto(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl());
    }
}
