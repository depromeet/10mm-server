package com.depromeet.domain.mission.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MissionControllerTest.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class MissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MissionService missionService;
    @Autowired private ObjectMapper objectMapper;
    // @Autowired
    // private DatabaseCleaner databaseCleaner;
    // @Autowired private MemberUtil memberUtil;
    //
    // @BeforeEach
    // void setUp() {
    // 	databaseCleaner.execute();
    // 	System.out.println(memberUtil.getCurrentMember().getId());
    // }

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
                                createRequest.name(),
                                createRequest.content(),
                                createRequest.category(),
                                createRequest.visibility()));
        // expected
        mockMvc.perform(
                        post("/missions")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.missionId").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").value("testMissionName"))
                .andExpect(jsonPath("$.content").value("testMissionContent"))
                .andExpect(jsonPath("$.category").value("STUDY"))
                .andExpect(jsonPath("$.visibility").value("ALL"))
                .andDo(print());
    }
}
