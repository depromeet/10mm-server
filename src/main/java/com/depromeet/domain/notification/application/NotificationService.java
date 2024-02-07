package com.depromeet.domain.notification.application;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(
            NotificationType notificationType, Member currentMember, Member targetMember) {
        Notification notification =
                Notification.createNotification(notificationType, currentMember, targetMember);
        notificationRepository.save(notification);
    }
}
