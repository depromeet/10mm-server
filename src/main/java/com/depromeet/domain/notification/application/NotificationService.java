package com.depromeet.domain.notification.application;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import com.depromeet.domain.notification.dto.NotificationFindAllResponse;
import com.depromeet.domain.notification.dto.request.PushMissionRemindRequest;
import com.depromeet.domain.notification.dto.request.PushUrgingSendRequest;
import com.depromeet.global.common.constants.PushNotificationConstants;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final MemberUtil memberUtil;
    private final FcmService fcmService;
    private final MissionRepository missionRepository;
    private final NotificationRepository notificationRepository;
    private final TaskScheduler taskScheduler;

    public void sendUrgingPush(PushUrgingSendRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Mission mission =
                missionRepository
                        .findById(request.missionId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        final Member targetMember = mission.getMember();

        validateSelfSending(currentMember.getId(), targetMember.getId());
        validateFinishedMission(mission);
        validateMissionNotCompletedToday(mission);

        fcmService.sendMessageSync(
                targetMember.getFcmInfo().getFcmToken(),
                PUSH_URGING_TITLE,
                String.format(
                        PushNotificationConstants.PUSH_URGING_CONTENT,
                        currentMember.getProfile().getNickname(),
                        mission.getName()));
        Notification notification =
                Notification.createNotification(
                        NotificationType.MISSION_URGING,
                        currentMember,
                        targetMember,
                        mission.getId());
        notificationRepository.save(notification);
    }

    public void sendMissionRemindPush(PushMissionRemindRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        // 10분 후에 실행되도록 작업을 예약
        taskScheduler.schedule(
                () ->
                        fcmService.sendMessageSync(
                                currentMember.getFcmInfo().getFcmToken(),
                                PUSH_MISSION_REMIND_TITLE,
                                PushNotificationConstants.PUSH_MISSION_REMIND_CONTENT),
                Instant.now().plusSeconds(request.seconds()));
    }

    public List<NotificationFindAllResponse> findAllNotification() {
        final Member member = memberUtil.getCurrentMember();
        return notificationRepository
                .findAllByTargetMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(
                        notification ->
                                NotificationFindAllResponse.of(
                                        notification.getId(),
                                        notification.getNotificationType(),
                                        notification.getResourceId(),
                                        getNotificationMessage(
                                                notification.getNotificationType(),
                                                notification.getSourceMember(),
                                                notification.getTargetMember()),
                                        notification.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private void validateFinishedMission(Mission mission) {
        if (mission.isFinished()) {
            throw new CustomException(ErrorCode.FINISHED_MISSION_URGING_NOT_ALLOWED);
        }
    }

    private void validateMissionNotCompletedToday(Mission mission) {
        if (mission.isCompletedMissionToday()) {
            throw new CustomException(ErrorCode.TODAY_COMPLETED_MISSION_SENDING_NOT_ALLOWED);
        }
    }

    private void validateSelfSending(Long currentMemberId, Long targetMemberId) {
        if (currentMemberId.equals(targetMemberId)) {
            throw new CustomException(ErrorCode.SELF_SENDING_NOT_ALLOWED);
        }
    }

    private String getNotificationMessage(
            NotificationType notificationType, Member sourceMember, Member targetMember) {
        switch (notificationType) {
            case FOLLOW:
                return String.format(PUSH_SERVICE_CONTENT, sourceMember.getProfile().getNickname());
            case MISSION_URGING:
                return String.format(
                        PUSH_URGING_CONTENT,
                        sourceMember.getProfile().getNickname(),
                        targetMember.getProfile().getNickname());
            default:
                throw new CustomException(ErrorCode.NOTIFICATION_TYPE_NOT_FOUND);
        }
    }
}
