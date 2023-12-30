package com.depromeet.domain.mission.controller;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import com.depromeet.domain.mission.service.MissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미션 API", description = "미션 관련 API입니다.")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController{

    private final MissionService missionService;

	@Operation(summary = "미션 생성", description = "미션을 생성합니다.")
	@PostMapping
    public Mission missionAdd(@Valid CreateMissionRequest createMissionRequest) {
        return missionService.addMission(createMissionRequest);
    }

	@Operation(summary = "미션 단건 조회", description = "미션을 한 개를 조회합니다.")
	@GetMapping("/{missionId}")
    public MissionResponse missionDetails(@PathVariable Long missionId) {
        return missionService.findMission(missionId);
    }

	@Operation(summary = "미션 리스트 조회", description = "미션 리스트를 조회합니다. (무한 스크롤)")
	@GetMapping
    public Slice<MissionResponse> missionList(
            @RequestParam int size, @RequestParam(required = false) Long lastId) {
        return missionService.listMission(size, lastId);
    }

	@Operation(summary = "미션 단건 수정", description = "단건 미션을 수정합니다.")
	@PutMapping("/{missionId}")
    public Mission missionModify(
            @Valid @RequestBody ModifyMissionRequest modifyMissionRequest,
            @PathVariable Long missionId) {
        return missionService.modifyMission(modifyMissionRequest, missionId);
    }

	@Operation(summary = "미션 단건 삭제", description = "단건 미션을 삭제합니다.")
	@DeleteMapping("/{missionId}")
    public void missionRemove(@PathVariable Long missionId) {
        missionService.removeMission(missionId);
    }
}
