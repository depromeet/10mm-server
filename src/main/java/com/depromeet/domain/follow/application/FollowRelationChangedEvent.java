package com.depromeet.domain.follow.application;

public record FollowRelationChangedEvent(Long sourceMemberId, Long targetMemberId) {}
