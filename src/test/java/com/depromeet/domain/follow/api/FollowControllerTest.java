package com.depromeet.domain.follow.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.depromeet.domain.follow.application.FollowService;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.global.error.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean
	private FollowService followService;

    @Nested
    class 팔로우를_추가할_때 {
        @Test
        void targetId가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            FollowCreateRequest request = new FollowCreateRequest(null);

            // when, then
            mockMvc.perform(
                            post("/follows")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("타겟 아이디는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // given
            FollowCreateRequest request = new FollowCreateRequest(21L);

            // when, then
            mockMvc.perform(
                            post("/follows")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                    .andDo(print());
        }
    }

    @Nested
    class 팔로우를_취소할_때 {
        @Test
        void targetId가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            FollowDeleteRequest request = new FollowDeleteRequest(null);

            // when, then
            mockMvc.perform(
                            delete("/follows")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("타겟 아이디는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // given
            FollowDeleteRequest request = new FollowDeleteRequest(21L);

            // when, then
            mockMvc.perform(
                            delete("/follows")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }
    }

    @Nested
    class 타인의_팔로우_카운트를_확인할_때 {
        @Test
        void targetId_타입이_일치하지_않은경우_예외가_발생한다() throws Exception {
            // given
            String targetId = "targetId";

            // when, then
            mockMvc.perform(get("/follows/{targetId}", targetId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentTypeMismatchException"))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getMessage()))
                    .andDo(print());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // given
            Long targetId = 215L;

            // when, then
            mockMvc.perform(get("/follows/{targetId}", targetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }
    }

    @Nested
    class 나의_팔로우_카운트를_확인할_때 {

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // when, then
            mockMvc.perform(get("/follows/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }
    }

    @Nested
    class 내가_팔로우한_유저_정보_리스트를_조회할_때 {
        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // when, then
            mockMvc.perform(get("/follows/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }
    }
}
