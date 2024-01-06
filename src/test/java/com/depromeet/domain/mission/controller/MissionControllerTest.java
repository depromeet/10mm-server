package com.depromeet.domain.mission.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.domain.mission.api.MissionController;
import com.depromeet.domain.mission.domain.ArchiveStatus;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = MissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class MissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MissionService missionService;

    /*
    무한 스크롤 방식 처리하는 메서드로
    MissionRepository에서 구현된 List<Mission>은 Repository 계층에서 이뤄지지에
    테스트 코드는 MissionFindResponse로 변경
    */
    private Slice<MissionFindResponse> checkLastPage(
            Pageable pageable, List<MissionFindResponse> result) {
        boolean hasNext = false;

        // 조회한 결과 개수가 요청한 페이지 사이즈보다 크면 뒤에 더 있음, next = true
        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
            result.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Test
    void 공부미션을_생성한다() throws Exception {
        // given
        MissionCreateRequest createRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        given(missionService.createMission(ArgumentMatchers.any()))
                .willReturn(
                        new MissionCreateResponse(
                                1L,
                                "testMissionName",
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.ALL));
        // expected
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

        // expected
        ResultActions perform =
                mockMvc.perform(
                        post("/missions")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(createRequest)));
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("{\"name\":\"이름은 비워둘 수 없습니다.\"}"))
                .andDo(print());
    }

    @Test
    void 미션_단건_조회한다() throws Exception {
        // given
        given(missionService.findOneMission(ArgumentMatchers.any()))
                .willReturn(
                        new MissionFindResponse(
                                1L,
                                "testMissionName",
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.ALL,
                                ArchiveStatus.NONE,
                                1));

        // expected
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
        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
        List<MissionFindResponse> mappedMissions =
                Arrays.asList(
                        new MissionFindResponse(
                                3L,
                                "testMissionName_3",
                                "testMissionContent_3",
                                MissionCategory.WRITING,
                                MissionVisibility.ALL,
                                ArchiveStatus.NONE,
                                3),
                        new MissionFindResponse(
                                2L,
                                "testMissionName_2",
                                "testMissionContent_2",
                                MissionCategory.ETC,
                                MissionVisibility.ALL,
                                ArchiveStatus.NONE,
                                2),
                        new MissionFindResponse(
                                1L,
                                "testMissionName_1",
                                "testMissionContent_1",
                                MissionCategory.STUDY,
                                MissionVisibility.ALL,
                                ArchiveStatus.NONE,
                                1));
        given(missionService.findAllMission(anyInt(), anyLong()))
                .willReturn(checkLastPage(pageRequest, mappedMissions));

        // expected
        ResultActions perform =
                mockMvc.perform(
                        get("/missions?size={size}&lastId={lastId}", size, lastId)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()));
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()", is(size)))
                .andExpect(jsonPath("$.data.content[0].missionId").value(size))
                .andExpect(jsonPath("$.data.last", is(true)))
                .andExpect(jsonPath("$.data.empty", is(false)));
    }

    @Test
    void 미션_공개여부를_팔로워로_수정한다() throws Exception {
        // given
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest(
                        "testMissionName", "testMissionContent", MissionVisibility.NONE);
        given(missionService.updateMission(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(
                        new MissionUpdateResponse(
                                1L,
                                "testMissionName",
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.FOLLOWER));

        // expected
        ResultActions perform =
                mockMvc.perform(
                        put("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(updateRequest)));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missionId").exists())
                .andExpect(
                        jsonPath("$.data.visibility").value(MissionVisibility.FOLLOWER.toString()))
                .andDo(print());
    }

    @Test
    void 미션이름을_null로_수정할_수_없다() throws Exception {
        // given
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest(null, "testMissionContent", MissionVisibility.NONE);
        given(missionService.updateMission(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(
                        new MissionUpdateResponse(
                                1L,
                                null,
                                "testMissionContent",
                                MissionCategory.STUDY,
                                MissionVisibility.FOLLOWER));

        // expected
        ResultActions perform =
                mockMvc.perform(
                        put("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(updateRequest)));

        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("{\"name\":\"이름은 비워둘 수 없습니다.\"}"))
                .andDo(print());
    }

    @Test
    void 미션_단건_삭제한다() throws Exception {
        // given
        Long missionId = 1L;
        doNothing().when(missionService).deleteMission(missionId);

        // expected
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
        doThrow(new EmptyResultDataAccessException(1))
                .when(missionService)
                .deleteMission(nonExistingMissionId);

        // then
        mockMvc.perform(
                        delete("/missions/{missionId}", nonExistingMissionId)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }
}
