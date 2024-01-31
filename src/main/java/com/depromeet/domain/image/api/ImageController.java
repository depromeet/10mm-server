package com.depromeet.domain.image.api;

import com.depromeet.domain.image.application.ImageService;
import com.depromeet.domain.image.dto.request.MemberProfileImageCreateRequest;
import com.depromeet.domain.image.dto.request.MemberProfileImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. [이미지]", description = "이미지 관련 API입니다.")
@RestController
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

    @Operation(
            summary = "회원 프로필 이미지 Presigned URL 생성",
            description = "회원 프로필 이미지 Presigned URL을 생성합니다.")
    @PostMapping("/members/me/upload-url")
    public PresignedUrlResponse memberProfilePresignedUrlCreate(
            @Valid @RequestBody MemberProfileImageCreateRequest request) {
        return imageService.createMemberProfilePresignedUrl(request);
    }

    @Deprecated
    @Operation(summary = "회원 프로필 이미지 업로드 완료처리 V1", description = "회원 프로필 이미지 업로드 완료 시 호출하시면 됩니다.")
    @PostMapping("/members/me/upload-complete")
    public void memberProfileUploaded(
            @Valid @RequestBody MemberProfileImageUploadCompleteRequest request) {
        imageService.uploadCompleteMemberProfile(request);
    }

    @Operation(
            summary = "회원 프로필 이미지 업로드 완료처리 V2",
            description = "V1과 동일합니다. 단, 요청 바디에 닉네임을 넣더라도 무시됩니다.")
    @PostMapping("/members/me/upload-complete/v2")
    public ResponseEntity<Void> memberProfileUploadedV2(
            @Valid @RequestBody MemberProfileImageUploadCompleteRequest request) {
        imageService.uploadCompleteMemberProfileV2(request);
        return ResponseEntity.ok().build();
    }
}
