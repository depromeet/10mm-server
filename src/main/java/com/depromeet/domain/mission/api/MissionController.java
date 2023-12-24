package com.depromeet.domain.mission.api;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.CreateMissionRequest;
import com.depromeet.domain.mission.dto.MissionResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.depromeet.global.config.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("")
    public Mission missionAdd(
		@Valid CreateMissionRequest createMissionRequest, PrincipalDetails details) {
        return missionService.addMission(createMissionRequest, details.getMemberId());
    }

    @GetMapping("")
    public Slice<MissionResponse> missionList(
            PrincipalDetails details,
            @RequestParam int size,
            @RequestParam(required = false) Long lastId) {
        return missionService.listMission(details.getMemberId(), size, lastId);
    }
}
