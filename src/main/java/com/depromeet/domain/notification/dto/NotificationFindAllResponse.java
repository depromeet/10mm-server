package com.depromeet.domain.notification.dto;

import com.depromeet.domain.mission.domain.*;
import com.depromeet.domain.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationFindAllResponse(
        @Schema(description = "알림 타입") NotificationType notificationType,
        @Schema(description = "알림 날짜") LocalDateTime createdAt) {

    public static NotificationFindAllResponse of(
            NotificationType notificationType, LocalDateTime createdAt) {
        return new NotificationFindAllResponse(notificationType, createdAt);
    }
}
