package com.depromeet.domain.missionrecord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageUploadStatus {
    NONE("업로드 없음"),
    PENDING("업로드 중"),
    COMPLETE("업로드 완료");

    private final String value;
}
