package com.depromeet.domain.notification.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    private Long sourceId;

    private Long targetId;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(Long sourceId, Long targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public static Notification createFollowNotification(Long sourceId, Long targetId) {
        return Notification.builder().sourceId(sourceId).targetId(targetId).build();
    }
}
