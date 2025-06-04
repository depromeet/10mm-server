package com.depromeet.domain.notification.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Member sourceMember;

    @ManyToOne
    @JoinColumn(name = "target_id")
    private Member targetMember;

    private Long resourceId;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(
            NotificationType notificationType,
            Member sourceMember,
            Member targetMember,
            Long resourceId) {
        this.notificationType = notificationType;
        this.sourceMember = sourceMember;
        this.targetMember = targetMember;
        this.resourceId = resourceId;
    }

    public static Notification createNotification(
            NotificationType notificationType,
            Member currentMember,
            Member targetMember,
            Long resourceId) {
        return Notification.builder()
                .notificationType(notificationType)
                .sourceMember(currentMember)
                .targetMember(targetMember)
                .resourceId(resourceId)
                .build();
    }
}
