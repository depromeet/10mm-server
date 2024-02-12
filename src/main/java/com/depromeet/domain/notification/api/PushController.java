package com.depromeet.domain.notification.api;

import com.depromeet.domain.notification.application.PushService;
import com.depromeet.domain.notification.dto.request.PushUrgingSendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. [알림]", description = "알림 관련 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class PushController {
    private final PushService pushService;

    @Operation(summary = "재촉하기", description = "당일 미션을 완료하지 않은 친구에게 재촉하기 Push Message를 발송합니다.")
    @PostMapping("/urging")
    public void urgingSend(@Valid @RequestBody PushUrgingSendRequest request) {
        pushService.sendUrgingPush(request);
    }
}
