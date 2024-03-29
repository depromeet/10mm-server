package com.depromeet.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record PushUrgingSendRequest(
        @NotNull(message = "missionId는 null일 수 없습니다.") Long missionId) {}
