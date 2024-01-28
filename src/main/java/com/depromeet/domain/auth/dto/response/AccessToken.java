package com.depromeet.domain.auth.dto.response;

import com.depromeet.domain.member.domain.MemberRole;

public record AccessToken(Long memberId, MemberRole memberRole) {}
