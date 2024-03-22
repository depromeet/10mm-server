package com.depromeet.scheduler.mission;

import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.notification.application.FcmService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
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

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = LocalTime.of(now.getHour(), now.getMinute());

        List<String> fcmTokenList = findFcmTokensForRemindPush(currentTime);
        fcmService.sendGroupMessageAsync(fcmTokenList, "미션 리마인드", "미션이 다가오고 있어요");
    }

    private List<String> findFcmTokensForRemindPush(LocalTime currentTime) {
        List<Mission> inProgressMissions = missionService.findInProgressMission();
        return inProgressMissions.stream()
                .filter(mission -> currentTime.equals(mission.getRemindedTime()))
                .map(mission -> mission.getMember().getFcmInfo().getFcmToken())
                .collect(Collectors.toList());
    }
}
