package com.depromeet.domain.notification.api;

import com.depromeet.domain.notification.application.NotificationService;
import com.depromeet.domain.notification.dto.NotificationFindAllResponse;
import com.depromeet.domain.notification.dto.request.PushMissionRemindRequest;
import com.depromeet.domain.notification.dto.request.PushUrgingSendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. [알림]", description = "알림 관련 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림센터 조회", description = "알림센터 목록을 조회합니다.")
    @GetMapping
    public List<NotificationFindAllResponse> notificationFindAll() {
        return notificationService.findAllNotification();
    }

    @Operation(summary = "재촉하기", description = "당일 미션을 완료하지 않은 친구에게 재촉하기 Push Message를 발송합니다.")
    @PostMapping("/urging")
    public void urgingSend(@Valid @RequestBody PushUrgingSendRequest request) {
        notificationService.sendUrgingPush(request);
    }

    @Operation(summary = "미션 타이머 리마인드 알림", description = "인증을 놓치는 경우에 대비하여 리마인드 알림을 전송합니다.")
    @PostMapping("/missions/remind")
    public void missionRemindSend(@Valid @RequestBody PushMissionRemindRequest request) {
        notificationService.sendMissionRemindPush(request);
    }
}
