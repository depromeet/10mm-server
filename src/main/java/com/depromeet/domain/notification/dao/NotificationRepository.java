package com.depromeet.domain.notification.dao;

import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findBySourceMemberIdAndTargetMemberIdAndNotificationType(
            Long sourceId, Long targetId, NotificationType notificationType);

    @EntityGraph(attributePaths = {"sourceMember", "targetMember"})
    List<Notification> findAllByTargetMemberIdOrderByCreatedAtDesc(Long targetMemberId);

    @Query(
            """
            select notification
            from Notification notification
            join fetch notification.sourceMember
            join fetch notification.targetMember
            where notification.targetMember.id = :targetMemberId
              and (
                    cast(:cursorCreatedAt as LocalDateTime) is null
                    or notification.createdAt < :cursorCreatedAt
                    or (
                        notification.createdAt = :cursorCreatedAt
                        and notification.id < :cursorNotificationId
                    )
              )
            order by notification.createdAt desc, notification.id desc
            """)
    List<Notification> findNotificationPage(
            @Param("targetMemberId") Long targetMemberId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorNotificationId") Long cursorNotificationId,
            Pageable pageable);
}
