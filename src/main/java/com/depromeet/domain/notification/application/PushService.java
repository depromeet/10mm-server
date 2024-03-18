package com.depromeet.domain.notification.application;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import com.depromeet.domain.notification.dto.request.PushMissionRemindRequest;
import com.depromeet.domain.notification.dto.request.PushUrgingSendRequest;
import com.depromeet.global.common.constants.PushNotificationConstants;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushService {
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
                        NotificationType.MISSION_URGING, currentMember, targetMember);
        notificationRepository.save(notification);
    }

    public void sendMissionRemindPush(PushMissionRemindRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Mission mission =
                missionRepository
                        .findById(request.missionId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        // 10분 후에 실행되도록 작업을 예약
        taskScheduler.schedule(
                () ->
                        fcmService.sendMessageSync(
                                currentMember.getFcmInfo().getFcmToken(),
                                PUSH_MISSION_REMIND_TITLE,
                                PushNotificationConstants.PUSH_MISSION_REMIND_CONTENT),
                Instant.now().plusSeconds(600)); // 10분 후에 실행
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
}
