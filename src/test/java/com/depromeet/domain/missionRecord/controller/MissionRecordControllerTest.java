package com.depromeet.domain.missionRecord.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.depromeet.domain.missionRecord.api.MissionRecordController;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindOneResponse;
import com.depromeet.domain.missionRecord.service.MissionRecordService;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MissionRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class MissionRecordControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MissionRecordService missionRecordService;
    @MockBean private MissionService missionService;

    MissionCreateResponse saveMission;

    @BeforeEach
    void setUp() {
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        saveMission = missionService.createMission(missionCreateRequest);
    }

    @Test
    void 미션_내역_생성한다() throws Exception {
        // given
        int durationMin = 32;
        int durationSec = 14;
        MissionRecordCreateRequest request =
                new MissionRecordCreateRequest(
                        1L,
                        LocalDateTime.of(2023, 1, 3, 0, 0, 0),
                        LocalDateTime.of(2023, 1, 3, 0, durationMin, durationSec),
                        durationMin,
                        durationSec);

		MissionRecordCreateResponse missionRecordCreateResponse = new MissionRecordCreateResponse(1L);
        given(missionRecordService.createMissionRecord(request)).willReturn(missionRecordCreateResponse);
        // expected
        ResultActions perform =
                mockMvc.perform(
                        post("/records")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(request)));

        perform.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.missionId").value(1L))
                .andDo(print());
    }

    @Test
    void 미션_내역_생성하는_데에_미션시간이_60분을_넘긴_경우_에러가_발생한다() throws Exception {
        // given
        int durationMin = 60;
        int durationSec = 29;
        MissionRecordCreateRequest request =
                new MissionRecordCreateRequest(
                        1L,
                        LocalDateTime.of(2023, 1, 3, 0, 0, 0),
                        LocalDateTime.of(2023, 1, 3, 1, 1, 2),
                        durationMin,
                        durationSec);

        given(missionRecordService.createMissionRecord(any()))
                .willThrow(new CustomException(ErrorCode.MISSION_RECORD_DURATION_OVERBALANCE));

        // expected
        ResultActions perform =
                mockMvc.perform(
                        post("/records")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(request)));

        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.data.message").value("미션 참여 시간이 지정 된 시간보다 초과하였습니다"))
                .andDo(print());
    }

    @Test
    void 미션_기록을_상세_조회한다() throws Exception {
		// given
		long duration = 21;
		long sinceDay = 3;
		LocalDateTime startedAt = LocalDateTime.of(2023, 1, 3, 0, 0, 0);
		LocalDateTime finishedAt = startedAt.plusMinutes(21);
		given(missionRecordService.findOneMissionRecord(1L))
			.willReturn(
				new MissionRecordFindOneResponse(
					1L,
					"testRemark",
					"https://ik.imagekit.io/demo/medium_cafe_B1iTdD0C.jpg", duration, sinceDay,
					startedAt,
					finishedAt));

		// then
		ResultActions perform = mockMvc.perform(
			get("/records/{recordId}", 1L)
			.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.with(csrf())
		);

		perform.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.recordId").value(1L))
			.andExpect(jsonPath("$.data.remark").value("testRemark"))
			.andExpect(jsonPath("$.data.duration").value(21))
			.andExpect(jsonPath("$.data.sinceDay").value(3));
    }
}
