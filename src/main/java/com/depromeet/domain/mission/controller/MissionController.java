package com.depromeet.domain.mission.controller;

import com.depromeet.domain.mission.api.MissionApi;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.depromeet.global.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MissionController implements MissionApi {

    private final MissionService missionService;
    private final SecurityUtil securityUtil;

    @Override
    public Mission missionAdd(@Valid CreateMissionRequest createMissionRequest) {
        return missionService.addMission(createMissionRequest, securityUtil.getCurrentMemberId());
    }

    @Override
    public Mission missionDetails(@PathVariable(value = "missionId") Long missionId) {
        return missionService.findMission(missionId);
    }

    @Override
    public Slice<MissionResponse> missionList(
            @RequestParam int size, @RequestParam(required = false) Long lastId) {
        return missionService.listMission(securityUtil.getCurrentMemberId(), size, lastId);
    }

    @Override
    public Mission missionModify(
            @Valid @RequestBody ModifyMissionRequest modifyMissionRequest,
            @PathVariable(value = "missionId") Long missionId) {
        return missionService.modifyMission(modifyMissionRequest, missionId);
    }

    @Override
    public void missionRemove(@PathVariable(value = "missionId") Long missionId) {
        missionService.removeMission(missionId);
    }
}
