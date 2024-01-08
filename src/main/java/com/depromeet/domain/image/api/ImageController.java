package com.depromeet.domain.image.api;

import com.depromeet.domain.image.application.ImageService;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3.[이미지]", description = "이미지 관련 API입니다.")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @Operation(
            summary = "미션 기록 이미지 Presigned URL 생성",
            description = "미션 기록 이미지 Presigned URL를 생성합니다.")
    @PostMapping("/records/upload-url")
    public PresignedUrlResponse missionRecordPresignedUrlCreate(
            @Valid @RequestBody MissionRecordImageCreateRequest request) {
        return imageService.createMissionRecordPresignedUrl(request);
    }

    @Operation(summary = "미션 기록 이미지 업로드 완료처리", description = "미션 기록 이미지 업로드 완료 시 호출하시면 됩니다.")
    @PostMapping("/records/upload-complete")
    public void missionRecordUploaded(
            @Valid @RequestBody MissionRecordImageUploadCompleteRequest request) {
        imageService.uploadCompleteMissionRecord(request);
    }
}
