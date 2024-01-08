package com.depromeet.domain.image.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.domain.ImageType;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SpringEnvironmentUtil;
import com.depromeet.infra.config.storage.StorageProperties;

import java.time.YearMonth;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final MemberUtil memberUtil;
    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final StorageProperties storageProperties;
    private final AmazonS3 amazonS3;
    private final MissionRecordRepository missionRecordRepository;

    @Transactional
    public PresignedUrlResponse createMissionRecordPresignedUrl(
            MissionRecordImageCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(request.missionRecordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        String fileName =
                createFileName(
                        ImageType.MISSION_RECORD,
                        request.missionRecordId(),
                        request.imageFileExtension());
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                createGeneratePreSignedUrlRequest(
                        storageProperties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();

        missionRecord.updateUploadStatusPending();
        return PresignedUrlResponse.from(presignedUrl);
    }

    @Transactional
    public void uploadCompleteMissionRecord(MissionRecordImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(request.missionRecordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
        String imageUrl =
                storageProperties.endpoint()
                        + "/"
                        + storageProperties.bucket()
                        + "/"
                        + springEnvironmentUtil.getCurrentProfile()
                        + "/"
                        + ImageType.MISSION_RECORD
                        + "/"
                        + request.missionRecordId()
                        + "/image."
                        + request.imageFileExtension().getUploadExtension();
        missionRecord.updateUploadStatusComplete(request.remark(), imageUrl);
    }

    private String createFileName(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension) {
        return springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/image."
                + imageFileExtension.getUploadExtension();
    }

    private GeneratePresignedUrlRequest createGeneratePreSignedUrlRequest(
            String bucket, String fileName, String fileExtension) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName, HttpMethod.PUT)
                        .withKey(fileName)
                        .withContentType("image/" + fileExtension)
                        .withExpiration(getPreSignedUrlExpiration());

        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL, CannedAccessControlList.PublicRead.toString());

        return generatePresignedUrlRequest;
    }

    private Date getPreSignedUrlExpiration() {
        Date expiration = new Date();
        var expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 30;
        expiration.setTime(expTimeMillis);
        return expiration;
    }
}