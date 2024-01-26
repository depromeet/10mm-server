package com.depromeet.domain.image.domain;

import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ImageFileExtension {
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png"),
    ;

    private final String uploadExtension;

            public static ImageFileExtension of(String uploadExtension) {
            return Arrays.stream(values())
                    .filter(imageFileExtension -> imageFileExtension.uploadExtension.equals(uploadExtension))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_FILE_EXTENSION_NOT_FOUND));
        }
}
