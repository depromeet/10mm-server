package com.depromeet.domain.follow.dto.request;

import jakarta.validation.constraints.NotNull;

public record FollowCreateRequest(@NotNull(message = "타겟 아이디는 비워둘 수 없습니다.") Long targetId) {}
