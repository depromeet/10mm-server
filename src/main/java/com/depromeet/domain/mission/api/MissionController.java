package com.depromeet.domain.mission.api;

import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.FollowMissionFindAllResponse;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindAllResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionSymbolStackResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2. [미션]", description = "미션 관련 API입니다.")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @Operation(summary = "미션 생성", description = "미션을 생성합니다.")
    @PostMapping
    public ResponseEntity<MissionCreateResponse> missionCreate(
            @Valid @RequestBody MissionCreateRequest missionCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(missionService.createMission(missionCreateRequest));
    }

    @Operation(summary = "미션 단건 조회", description = "미션을 한 개를 조회합니다.")
    @GetMapping("/{missionId}")
    public MissionFindResponse missionFindOne(@PathVariable Long missionId) {
        return missionService.findOneMission(missionId);
    }

    @Operation(summary = "미션 리스트 조회", description = "미션 리스트를 조회합니다.")
    @GetMapping
    public List<MissionFindAllResponse> missionFindAll() {
        return missionService.findAllMission();
    }

    @Operation(summary = "친구 미션 목록 조회", description = "친구 미션 목록을 조회합니다.")
    @GetMapping("/follow/{targetId}")
    public FollowMissionFindAllResponse followMissionFindAll(@PathVariable Long targetId) {
        return missionService.findAllFollowMissions(targetId);
    }

    @Operation(summary = "미션 전체 현황", description = "미션 전체 현황을 조회합니다.")
    @GetMapping("/summary")
    public MissionRecordSummaryResponse missionRecordFindSummary() {
        return missionService.findSummaryMissionRecord();
    }

	@Operation(summary = "번개 스택 조회", description = "완료한 미션 대상으로 번개 스택을 조회합니다.")
	@GetMapping("/symbol/{memberId}")
	public MissionSymbolStackResponse missionSymbolStackFind(@PathVariable Long memberId) {
		return missionService.findMissionSymbolStack(memberId);
	}

    @Operation(summary = "미션 단건 수정", description = "단건 미션을 수정합니다.")
    @PutMapping("/{missionId}")
    public MissionUpdateResponse missionUpdate(
            @Valid @RequestBody MissionUpdateRequest missionUpdateRequest,
            @PathVariable Long missionId) {
        return missionService.updateMission(missionUpdateRequest, missionId);
    }

    @Operation(summary = "미션 단건 삭제", description = "단건 미션을 삭제합니다.")
    @DeleteMapping("/{missionId}")
    public void missionDelete(@PathVariable Long missionId) {
        missionService.deleteMission(missionId);
    }
}
