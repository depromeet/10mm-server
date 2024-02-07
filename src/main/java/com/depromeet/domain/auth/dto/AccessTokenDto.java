package com.depromeet.domain.auth.dto;

import com.depromeet.domain.member.domain.MemberRole;

public record AccessTokenDto(Long memberId, MemberRole memberRole, String tokenValue) {}
