package com.depromeet.domain.image.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.domain.image.application.ImageService;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ImageService imageService;

    @Nested
    class 미션_기록_이미지_PresignedUrl을_생성할_때 {
        @Test
        void 미션_ID가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(null, ImageFileExtension.JPEG);

            // when, then
            mockMvc.perform(
                            post("/records/upload-url")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("미션 기록 ID는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 이미지_파일의_확장자가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            MissionRecordImageCreateRequest request = new MissionRecordImageCreateRequest(1L, null);

            // when, then
            mockMvc.perform(
                            post("/records/upload-url")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("이미지 파일의 확장자는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // given
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(30L, ImageFileExtension.JPEG);
            PresignedUrlResponse response = PresignedUrlResponse.from("presignedUrl");

            // when
            given(imageService.createMissionRecordPresignedUrl(request)).willReturn(response);

            // then
            mockMvc.perform(
                            post("/records/upload-url")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.presignedUrl").value("presignedUrl"))
                    .andDo(print());
        }
    }

    @Nested
    class 미션_기록_이미지_업로드_완료_처리할_때 {
        @Test
        void 미션_ID가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(
                            null, ImageFileExtension.JPEG, "미션 일지");

            // when, then
            mockMvc.perform(
                            post("/records/upload-complete")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("미션 기록 ID는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 이미지_파일의_확장자가_NULL_이라면_예외를_발생시킨다() throws Exception {
            // given
            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(182L, null, "미션 일지");

            // when, then
            mockMvc.perform(
                            post("/records/upload-complete")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(
                            jsonPath("$.data.errorClassName")
                                    .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.data.message").value("이미지 파일의 확장자는 비워둘 수 없습니다."))
                    .andDo(print());
        }

        @Test
        void 미션_일지는_NULL_이라도_예외가_발생하지_않는다() throws Exception {
            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(
                            182L, ImageFileExtension.JPEG, null);

            // when, then
            mockMvc.perform(
                            post("/records/upload-complete")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() throws Exception {
            // given
            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(
                            182L, ImageFileExtension.JPEG, "미션 일지");

            // when, then
            mockMvc.perform(
                            post("/records/upload-url")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andDo(print());
        }
    }
}
