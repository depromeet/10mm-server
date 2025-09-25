package com.depromeet.global.util;

import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.infra.config.s3.S3Properties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUtil {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @SneakyThrows
    public String createPreSignedUrl(String fileName, ImageFileExtension fileExtension) {
        PutObjectPresignRequest request =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(
                                builder ->
                                        builder.bucket(s3Properties.bucket())
                                                .key(fileName)
                                                .contentType(getContentType(fileExtension))
                                                .acl(ObjectCannedACL.PUBLIC_READ))
                        .build();
        return s3Presigner.presignPutObject(request).url().toString();
    }

    private static String getContentType(ImageFileExtension fileExtension) {
        return "image/" + fileExtension.getUploadExtension();
    }
}
