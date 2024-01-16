package com.depromeet.domain.mission.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.api.MissionController;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.domain.ArchiveStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.*;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean({JpaMetamodelMappingContext.class, JwtAuthenticationFilter.class})
class MissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MissionService missionService;

    @Test
    void 공부미션을_생성한다() throws Exception {
        // given
        MissionCreateRequest createRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        given(missionService.createMission(any()))
                .willReturn(
                        new MissionCreateResponse(
                                1L,
                                "testMissionName",
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.ALL));
        // when, then
        ResultActions perform =
                mockMvc.perform(
                        post("/missions")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(createRequest)));

        perform.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.missionId").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.name").value("testMissionName"))
                .andExpect(jsonPath("$.data.content").value("testMissionContent"))
                .andExpect(jsonPath("$.data.category").value("STUDY"))
                .andExpect(jsonPath("$.data.visibility").value("ALL"))
                .andDo(print());
    }

    @Test
    void 미션_생성하는데_이름은_null일_수_없다() throws Exception {
        // given
        MissionCreateRequest createRequest =
                new MissionCreateRequest(
                        null, "testMissionContent", MissionCategory.STUDY, MissionVisibility.ALL);

        // when, then
        ResultActions perform =
                mockMvc.perform(
                        post("/missions")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(createRequest)));
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(
                        jsonPath("$.data.errorClassName").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.data.message").value("이름은 비워둘 수 없습니다."))
                .andDo(print());
    }

    @Test
    void 미션_단건_조회한다() throws Exception {
        // given
        given(missionService.findOneMission(any()))
                .willReturn(
                        new MissionFindResponse(
                                1L,
                                "testMissionName",
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.ALL,
                                ArchiveStatus.NONE,
                                1));

        // when, then
        ResultActions perform =
                mockMvc.perform(
                        get("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missionId").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.name").value("testMissionName"));
    }

    @Test
    void 미션_리스트를_조회한다() throws Exception {
        // given
        int size = 3;
        long lastId = 4;
        LocalDateTime ttlFinishedAt = LocalDateTime.now().plusMinutes(10);
        Member member =
                Member.createNormalMember(Profile.createProfile("testNickname", "testImageUrl"));
        LocalDateTime missionStartedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime missionFinishedAt = missionStartedAt.plusWeeks(2);
        Mission mission =
                Mission.createMission(
                        "testMissionName_1",
                        "testMissionContent_1",
                        1,
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        missionStartedAt,
                        missionFinishedAt,
                        member);

        List<MissionFindAllResponse> missionList =
                Arrays.asList(
                        MissionFindAllResponse.of(mission, MissionStatus.NONE, null, null),
                        MissionFindAllResponse.of(mission, MissionStatus.COMPLETED, null, null),
                        MissionFindAllResponse.of(
                                mission, MissionStatus.REQUIRED, ttlFinishedAt, null));
        given(missionService.findAllMission()).willReturn(missionList);

        // when, then
        ResultActions perform =
                mockMvc.perform(
                        get("/missions")
                                .param("size", String.valueOf(size))
                                .param("lastId", String.valueOf(lastId))
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()));
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(size)))
                .andDo(print());
    }

    @Test
    void 미션_공개여부를_팔로워로_수정한다() throws Exception {
        // given
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest(
                        "testMissionName", "testMissionContent", MissionVisibility.NONE);
        given(missionService.updateMission(any(), any())).willReturn(new MissionUpdateResponse(1L));

        // when, then
        ResultActions perform =
                mockMvc.perform(
                        put("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(updateRequest)));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missionId").exists())
                .andExpect(jsonPath("$.data.missionId").value(1L))
                .andDo(print());
    }

    @Test
    void 미션이름을_null로_수정할_수_없다() throws Exception {
        // given
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest(null, "testMissionContent", MissionVisibility.NONE);
        given(missionService.updateMission(any(), any())).willReturn(new MissionUpdateResponse(1L));

        // when, then
        ResultActions perform =
                mockMvc.perform(
                        put("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(updateRequest)));

        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(
                        jsonPath("$.data.errorClassName").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.data.message").value("이름은 비워둘 수 없습니다."))
                .andDo(print());
    }

    @Test
    void 미션_단건_삭제한다() throws Exception {
        // given
        Long missionId = 1L;
        doNothing().when(missionService).deleteMission(missionId);

        // when, then
        mockMvc.perform(
                        delete("/missions/{missionId}", missionId)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_미션을_삭제한다() throws Exception {
        // given
        Long nonExistingMissionId = 999L;

        // when
        doThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND))
                .when(missionService)
                .deleteMission(nonExistingMissionId);

        // then
        mockMvc.perform(
                        delete("/missions/{missionId}", nonExistingMissionId)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
