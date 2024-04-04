package com.depromeet.scheduler.mission;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dto.response.MissionRemindPushResponse;
import com.depromeet.domain.notification.application.FcmService;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionBatchScheduler {
    private final MissionService missionService;
    private final FcmService fcmService;

    // 자정에 schedule 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updateFinishedDurationStatus() {
        log.info("DurationStatus Update batch execute");
        missionService.updateFinishedDurationStatus();
    }

    // 매 10분마다 schedule 실행
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void missionRemindPushNotification() {
        log.info("Mission Remind Push Notification batch execute");

        List<MissionRemindPushResponse> inProgressMissions =
                missionService.findAllInProgressMission();
        inProgressMissions.forEach(this::sendMissionRemindPushNotification);
    }

    private void sendMissionRemindPushNotification(MissionRemindPushResponse remindPushResponse) {
        LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (currentTime.equals(remindPushResponse.remindAt())) {
            fcmService.sendMessageSync(
                    remindPushResponse.fcmToken(),
                    PUSH_MISSION_START_REMIND_TITLE,
                    String.format(PUSH_MISSION_START_REMIND_CONTENT, remindPushResponse.name()));
        }
    }
}
