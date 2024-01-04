package com.depromeet.domain.mission.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.mission.api.MissionController;
import com.depromeet.domain.mission.domain.ArchiveStatus;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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
    @MockBean private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
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

    // TODO: validation 의존성 생기면 진행
    // @Test
    // void 미션_생성하는_데에_이름은_null일_수_없다() throws Exception {
    // 	// given
    // 	MissionCreateRequest createRequest =
    // 		new MissionCreateRequest(
    // 			"",
    // 			"testMissionContent",
    // 			MissionCategory.STUDY,
    // 			MissionVisibility.ALL);
    //
    // 	// expected
    // 	mockMvc.perform(
    // 			post("/missions")
    // 				.accept(APPLICATION_JSON)
    // 				.contentType(APPLICATION_JSON)
    // 				.with(csrf())
    // 				.content(objectMapper.writeValueAsString(createRequest)))
    // 		.andExpect(status().isBadRequest())
    //
    // 		.andDo(print());
    // }

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
        mockMvc.perform(
                        get("/missions/{missionId}", 1L)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missionId").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.name").value("testMissionName"));
    }

    @Test
    void 미션_리스트를_조회한다() throws Exception {
        // given
        given(missionService.findAllMission(anyInt(), anyLong()))
                .willReturn(
                        new SliceImpl<>(
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
                                                1)),
                                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id")),
                                false));

        // expected
        mockMvc.perform(
                        get("/missions?size=3&lastId=4")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()", is(3)))
                .andExpect(jsonPath("$.data.content[0].missionId").value(3))
                .andExpect(jsonPath("$.data.last", is(true)))
                .andExpect(jsonPath("$.data.empty", is(false)));
    }
}
