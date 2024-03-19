package com.depromeet.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record PushMissionRemindRequest(@NotNull(message = "시간은 비울 수 없습니다.") Long seconds) {}
