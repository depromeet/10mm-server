package com.depromeet.scheduler.mission;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.notification.application.FcmService;
import java.time.LocalTime;
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
    @Scheduled(cron = "0 0/10 * * * *", zone = "Asia/Seoul")
    public void missionRemindPushNotification() {
        log.info("Mission Remind Push Notification batch execute");

        LocalTime now = LocalTime.now();

        List<Mission> inProgressMissions = missionService.findAllInProgressMission();
        sendFcmTokensForRemindPush(now, inProgressMissions);
    }

    private void sendFcmTokensForRemindPush(
            LocalTime currentTime, List<Mission> inProgressMissions) {
        inProgressMissions.stream()
                .filter(mission -> currentTime.equals(mission.getRemindAt()))
                .forEach(
                        mission -> {
                            String fcmToken = mission.getMember().getFcmInfo().getFcmToken();
                            fcmService.sendMessageSync(
                                    fcmToken,
                                    PUSH_MISSION_START_REMIND_TITLE,
                                    String.format(
                                            PUSH_MISSION_START_REMIND_CONTENT, mission.getName()));
                        });
    }
}
