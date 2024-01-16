package com.depromeet.domain.missionRecord.api;

import com.depromeet.domain.missionRecord.application.MissionRecordService;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordUpdateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindOneResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3. [미션 기록]", description = "미션 기록 관련 API")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {
    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 기록 생성", description = "미션 기록을 생성하고 생성 된 id를 반환합니다.")
    @PostMapping
    public ResponseEntity<MissionRecordCreateResponse> missionRecordCreate(
            @Valid @RequestBody MissionRecordCreateRequest request) {
        MissionRecordCreateResponse response = missionRecordService.createMissionRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "미션 기록 조회", description = "미션 기록을 조회합니다.")
    @GetMapping("/{recordId}")
    public MissionRecordFindOneResponse missionRecordFindOne(@PathVariable Long recordId) {
        return missionRecordService.findOneMissionRecord(recordId);
    }

    @Operation(summary = "미션 기록 조회 (캘린더 뷰)", description = "미션 기록을 조회합니다.")
    @GetMapping
    public List<MissionRecordFindResponse> missionRecordFind(
            @RequestParam("missionId") Long missionId,
            @RequestParam("yearMonth") YearMonth yearMonth) {
        return missionRecordService.findAllMissionRecord(missionId, yearMonth);
    }

    @Operation(summary = "미션 기록 단건 수정", description = "미션 기록을 수정합니다.")
    @PutMapping("/{recordId}")
    public MissionRecordUpdateResponse missionRecordUpdate(
            @Valid @RequestBody MissionRecordUpdateRequest request, @PathVariable Long recordId) {
        return missionRecordService.updateMissionRecord(request, recordId);
    }

    @Operation(
            summary = "이미 진행중인 미션 기록들 삭제",
            description = "이미 진행중인 미션 기록들을 삭제합니다. (인증 필요인 경우만 삭제)")
    @DeleteMapping("/in-progress")
    public ResponseEntity<Void> missionRecordInProgressDelete() {
        missionRecordService.deleteInProgressMissionRecord();
        return ResponseEntity.ok().build();
    }
}
