package com.depromeet.domain.image.dto.request;

import com.depromeet.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MissionRecordImageUploadCompleteRequest(
        @NotNull(message = "미션 기록 ID는 비워둘 수 없습니다.")
                @Schema(description = "미션 기록 ID", defaultValue = "1")
                Long missionRecordId,
        @NotNull(message = "이미지 파일의 확장자는 비워둘 수 없습니다.")
                @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @Size(max = 200, message = "미션 일지는 20자 이하까지만 입력 가능합니다.") String remark) {}
