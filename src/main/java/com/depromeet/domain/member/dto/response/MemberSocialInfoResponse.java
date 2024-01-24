package com.depromeet.domain.member.dto.response;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.member.domain.Member;

public record MemberSocialInfoResponse(OauthProvider provider, String email) {
    public static MemberSocialInfoResponse from(Member member) {
        return new MemberSocialInfoResponse(
                OauthProvider.of(member.getOauthInfo().getOauthProvider()),
                member.getOauthInfo().getOauthEmail());
    }
}
