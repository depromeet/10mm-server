package com.depromeet.domain.notification.dao;

import com.depromeet.domain.notification.domain.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findBySourceMemberIdAndTargetMemberId(
            Long sourceMemberId, Long targetMemberId);
}
