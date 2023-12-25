package com.depromeet.domain.mission.api;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.depromeet.global.config.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미션 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/missions")
public class MissionController {

    private final MissionService missionService;
    private final MemberRepository memberRepository;

    @PostMapping("")
    public Mission missionAdd(
            @Valid CreateMissionRequest createMissionRequest, PrincipalDetails details) {
        return missionService.addMission(createMissionRequest, details.getMemberId());
    }

    @GetMapping("/{missionId}")
    public Mission missionDetails(@PathVariable(value = "missionId") Long missionId) {
        return missionService.findMission(missionId);
    }

    @GetMapping("")
    public Slice<MissionResponse> missionList(
            PrincipalDetails details,
            @RequestParam int size,
            @RequestParam(required = false) Long lastId) {
        return missionService.listMission(details.getMemberId(), size, lastId);
    }

    @PatchMapping("/{missionId}")
    public Mission missionModify(
            @Valid ModifyMissionRequest modifyMissionRequest,
            @PathVariable(value = "missionId") Long missionId) {
        return missionService.modifyMission(modifyMissionRequest, missionId);
    }

    @DeleteMapping("/{missionId}")
    public void missionRemove(@PathVariable(value = "missionId") Long missionId) {
        missionService.removeMission(missionId);
    }
}
