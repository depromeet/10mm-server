package com.depromeet.scheduler.member;

import com.depromeet.domain.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberBatchScheduler {



    private final MemberService memberService;







    @Scheduled(cron = "0 0 22 * * *")
    public void pushNotificationByMissionRequest() {
        log.info("PushNotification MissionRequest execute");
        memberService.pushNotificationMissionRequest();
    }
}
