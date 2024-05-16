package com.depromeet.domain.image.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.depromeet.domain.image.dao.ImageRepository;
import com.depromeet.domain.image.domain.Image;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.domain.ImageType;
import com.depromeet.domain.image.dto.request.MemberProfileImageCreateRequest;
import com.depromeet.domain.image.dto.request.MemberProfileImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.dao.MissionRecordTtlRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.common.constants.UrlConstants;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SpringEnvironmentUtil;
import com.depromeet.infra.config.s3.S3Properties;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
    private final MemberUtil memberUtil;
    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final S3Properties s3Properties;
    private final AmazonS3 amazonS3;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRecordTtlRepository missionRecordTtlRepository;
    private final ImageRepository imageRepository;

    public PresignedUrlResponse createMissionRecordPresignedUrl(
            MissionRecordImageCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        MissionRecord missionRecord = findMissionRecordById(request.missionRecordId());

        Mission mission = missionRecord.getMission();
        validateMissionRecordUserMismatch(mission, currentMember);

        String imageKey = generateUUID();
        String fileName =
                createFileName(
                        ImageType.MISSION_RECORD,
                        request.missionRecordId(),
                        imageKey,
                        request.imageFileExtension());
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                createGeneratePreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();

        missionRecord.updateUploadStatusPending();
        missionRecordTtlRepository.deleteById(request.missionRecordId());
        imageRepository.save(
                Image.createImage(
                        ImageType.MISSION_RECORD,
                        request.missionRecordId(),
                        imageKey,
                        request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl);
    }

    public void uploadCompleteMissionRecord(MissionRecordImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        MissionRecord missionRecord = findMissionRecordById(request.missionRecordId());

        Mission mission = missionRecord.getMission();
        validateMissionRecordUserMismatch(mission, currentMember);

        Image image =
                findImage(
                        ImageType.MISSION_RECORD,
                        request.missionRecordId(),
                        request.imageFileExtension());
        String imageUrl =
                createReadImageUrl(
                        ImageType.MISSION_RECORD,
                        request.missionRecordId(),
                        image.getImageKey(),
                        request.imageFileExtension());
        missionRecord.updateUploadStatusComplete(request.remark(), imageUrl);
    }

    public PresignedUrlResponse createMemberProfilePresignedUrl(
            MemberProfileImageCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        String imageKey = generateUUID();
        String fileName =
                createFileName(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension());
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                createGeneratePreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        imageRepository.save(
                Image.createImage(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl);
    }

    public void uploadCompleteMemberProfile(MemberProfileImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        String imageUrl = null;
        if (request.imageFileExtension() != null) {
            Image image =
                    findImage(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            request.imageFileExtension());
            imageUrl =
                    createReadImageUrl(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            image.getImageKey(),
                            request.imageFileExtension());
        }
        currentMember.updateProfile(Profile.createProfile(request.nickname(), imageUrl));
    }

    public void uploadCompleteMemberProfileV2(MemberProfileImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        String imageUrl = null;
        if (request.imageFileExtension() != null) {
            Image image =
                    findImage(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            request.imageFileExtension());
            imageUrl =
                    createReadImageUrl(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            image.getImageKey(),
                            request.imageFileExtension());
        }
        // 닉네임 변경은 무시됩니다 (현재 닉네임을 그대로 사용)
        String currentNickname = currentMember.getProfile().getNickname();
        currentMember.updateProfile(Profile.createProfile(currentNickname, imageUrl));
    }

    private Image findImage(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension) {
        return imageRepository
                .queryImageKey(imageType, targetId, imageFileExtension)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_KEY_NOT_FOUND));
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private MissionRecord findMissionRecordById(Long request) {
        return missionRecordRepository
                .findById(request)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
    }

    private String createFileName(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
                + imageFileExtension.getUploadExtension();
    }

    private String createUploadImageUrl(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return s3Properties.endpoint()
                + "/"
                + s3Properties.bucket()
                + "/"
                + springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
                + imageFileExtension.getUploadExtension();
    }

    private String createReadImageUrl(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return UrlConstants.IMAGE_DOMAIN_URL.getValue()
                + "/"
                + springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
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

    private void validateMissionRecordUserMismatch(Mission mission, Member member) {
        if (!mission.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.MISSION_RECORD_USER_MISMATCH);
        }
    }
}
