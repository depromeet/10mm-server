package com.depromeet.domain.missionRecord.api;

import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindResponse;
import com.depromeet.domain.missionRecord.service.MissionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[미션 기록]", description = "미션 기록 관련 API")
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

    @Operation(summary = "미션 기록 조회", description = "미션 기록을 조회합니다.")
    @GetMapping
    public List<MissionRecordFindResponse> missionRecordFind(
            @RequestParam("missionId") Long missionId,
            @RequestParam("yearMonth") YearMonth yearMonth) {
        return missionRecordService.findAllMissionRecord(missionId, yearMonth);
    }
}
