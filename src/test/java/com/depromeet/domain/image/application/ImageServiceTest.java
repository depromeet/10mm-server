package com.depromeet.domain.image.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.request.MissionRecordImageUploadCompleteRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.missionRecord.application.MissionRecordService;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ImageServiceTest {
    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ImageService imageService;
    @Autowired private MissionRecordService missionRecordService;
    @Autowired private MissionService missionService;
    @Autowired private MissionRecordRepository missionRecordRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    class 미션_기록_이미지_PresignedUrl을_생성할_때 {
         @Test
         void 회원이_존재하지_않는다면_예외를_발생시킨다() {
         	// given
         	MissionRecordImageCreateRequest request =
         		new MissionRecordImageCreateRequest(192L, ImageFileExtension.JPEG);

         	// when, then
         	assertThatThrownBy(() -> imageService.createMissionRecordPresignedUrl(request))
         		.isInstanceOf(CustomException.class)
         		.hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
         }

        @Test
        void 미션이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname", "testImageUrl")));
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(192L, ImageFileExtension.JPEG);

            // when, then
            assertThatThrownBy(() -> imageService.createMissionRecordPresignedUrl(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MISSION_RECORD_NOT_FOUND.getMessage());
        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname", "testImageUrl")));
            MissionCreateRequest missionCreateRequest =
                    new MissionCreateRequest(
                            "testMissionName",
                            "testMissionContent",
                            MissionCategory.STUDY,
                            MissionVisibility.ALL);
            MissionCreateResponse missionCreateResponse =
                    missionService.createMission(missionCreateRequest);

            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            MissionRecordCreateRequest missionRecordCreateRequest =
                    new MissionRecordCreateRequest(
                            missionCreateResponse.missionId(),
                            missionRecordStartedAt,
                            missionRecordFinishedAt,
                            32,
                            14);

            MissionRecordCreateResponse missionRecordCreateResponse =
                    missionRecordService.createMissionRecord(missionRecordCreateRequest);
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(
                            missionRecordCreateResponse.missionId(), ImageFileExtension.JPEG);

            // when, then
            assertThatCode(() -> imageService.createMissionRecordPresignedUrl(request))
                    .doesNotThrowAnyException();
        }

        @Test
        void 입력_값이_정상이라면_PresignedUrl이_정상적으로_생성된다() {
            // given
            Member member =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname", "testImageUrl")));
            MissionCreateRequest missionCreateRequest =
                    new MissionCreateRequest(
                            "testMissionName",
                            "testMissionContent",
                            MissionCategory.STUDY,
                            MissionVisibility.ALL);
            MissionCreateResponse missionCreateResponse =
                    missionService.createMission(missionCreateRequest);

            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            MissionRecordCreateRequest missionRecordCreateRequest =
                    new MissionRecordCreateRequest(
                            missionCreateResponse.missionId(),
                            missionRecordStartedAt,
                            missionRecordFinishedAt,
                            32,
                            14);
            MissionRecordCreateResponse missionRecordCreateResponse =
                    missionRecordService.createMissionRecord(missionRecordCreateRequest);
            ImageFileExtension imageFileExtension = ImageFileExtension.JPEG;
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(
                            missionRecordCreateResponse.missionId(), imageFileExtension);

            // when
            PresignedUrlResponse missionRecordPresignedUrl =
                    imageService.createMissionRecordPresignedUrl(request);

            // then
            assertThat(missionRecordPresignedUrl.presignedUrl())
                    .containsPattern(
                            String.format(
                                    "/local/mission_record/%s/.*\\.jpeg",
                                    missionRecordCreateResponse.missionId()));
        }
    }

    @Nested
    class 미션_기록_이미지_업로드_완료_처리할_때 {
         @Test
         void 회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
             MissionRecordImageUploadCompleteRequest request =
                new MissionRecordImageUploadCompleteRequest(192L, ImageFileExtension.JPEG,
 "testRemark");

            // when, then
            assertThatThrownBy(() -> imageService.uploadCompleteMissionRecord(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
         }

        @Test
        void 미션이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname", "testImageUrl")));
            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(
                            192L, ImageFileExtension.JPEG, "testRemark");

            // when, then
            assertThatThrownBy(() -> imageService.uploadCompleteMissionRecord(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MISSION_RECORD_NOT_FOUND.getMessage());
        }

        @Test
        void 입력_값이_정상이라면_미션_이미지_업로드_완료처리가_된다() {
            // given
            Member member =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname", "testImageUrl")));
            MissionCreateRequest missionCreateRequest =
                    new MissionCreateRequest(
                            "testMissionName",
                            "testMissionContent",
                            MissionCategory.STUDY,
                            MissionVisibility.ALL);
            MissionCreateResponse missionCreateResponse =
                    missionService.createMission(missionCreateRequest);

            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            MissionRecordCreateRequest missionRecordCreateRequest =
                    new MissionRecordCreateRequest(
                            missionCreateResponse.missionId(),
                            missionRecordStartedAt,
                            missionRecordFinishedAt,
                            32,
                            14);
            MissionRecordCreateResponse missionRecordCreateResponse =
                    missionRecordService.createMissionRecord(missionRecordCreateRequest);

            ImageFileExtension imageFileExtension = ImageFileExtension.JPEG;
            MissionRecordImageCreateRequest missionRecordImageCreateRequest =
                    new MissionRecordImageCreateRequest(
                            missionRecordCreateResponse.missionId(), imageFileExtension);
            imageService.createMissionRecordPresignedUrl(missionRecordImageCreateRequest);

            MissionRecordImageUploadCompleteRequest request =
                    new MissionRecordImageUploadCompleteRequest(
                            missionRecordCreateResponse.missionId(),
                            imageFileExtension,
                            "testRemark");

            // when
            imageService.uploadCompleteMissionRecord(request);
            MissionRecord missionRecord =
                    missionRecordRepository.findById(missionRecordCreateResponse.missionId()).get();

            // then
            assertThat(missionRecord.getUploadStatus()).isEqualTo(ImageUploadStatus.COMPLETE);
            assertThat(missionRecord.getRemark()).isEqualTo("testRemark");
            assertThat(missionRecord.getImageUrl())
                    .containsPattern(
                            String.format(
                                    "/local/mission_record/%s/.*\\.jpeg",
                                    missionRecordCreateResponse.missionId()));
        }
    }
}
