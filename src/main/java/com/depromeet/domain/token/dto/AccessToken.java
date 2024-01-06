package com.depromeet.domain.token.dto;

import com.depromeet.domain.member.domain.MemberRole;

public record AccessToken(Long memberId, MemberRole memberRole) {}
