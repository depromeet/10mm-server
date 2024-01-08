package com.depromeet.domain.missionRecord.api;

import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.service.MissionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. [미션 기록]", description = "미션 기록 관련 API")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {
    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 기록 생성", description = "미션 기록을 생성하고 생성 된 id를 반환합니다.")
    @PostMapping
    public ResponseEntity<Long> missionRecordCreate(
            @Valid @RequestBody MissionRecordCreateRequest request) {
        Long missionRecordId = missionRecordService.createMissionRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(missionRecordId);
    }
}
