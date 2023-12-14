package com.depromeet.domain.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Profile(String nickname, String profileImageUrl) {}
