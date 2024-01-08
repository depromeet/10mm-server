package com.depromeet.domain.image.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.service.MissionService;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.service.MissionRecordService;
import com.depromeet.global.config.security.PrincipalDetails;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
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

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @Nested
    class 미션_기록_이미지_PresignedUrl을_생성할_때 {
        // TODO: MemberUtil insertMockMemberIfNotExist메서드 제거 후 주석해제 예정
        // @Test
        // void 회원이_존재하지_않는다면_예외를_발생시킨다() {
        // 	// given
        // 	MissionRecordImageCreateRequest request =
        // 		new MissionRecordImageCreateRequest(192L, ImageFileExtension.JPEG);
        //
        // 	// when, then
        // 	assertThatThrownBy(() -> imageService.createMissionRecordPresignedUrl(request))
        // 		.isInstanceOf(CustomException.class)
        // 		.hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        // }

        @Test
        void 미션이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(new Profile("testNickname", "testImageUrl")));
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(192L, ImageFileExtension.JPEG);

            // when, then
            assertThatThrownBy(() -> imageService.createMissionRecordPresignedUrl(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MISSION_RECORD_NOT_FOUND.getMessage());
        }

//        TODO: SecurityUtil setMockAuthentication메서드 제거 후 주석해제 예정
//        @Test
//        void 미션을_생성한_유저와_로그인_유저가_일치하지_않는다면_예외를_발생시킨다() {
//            // given
//            memberRepository.save(
//                    Member.createNormalMember(new Profile("testNickname", "testImageUrl")));
//            MissionCreateRequest missionCreateRequest =
//                    new MissionCreateRequest(
//                            "testMissionName",
//                            "testMissionContent",
//                            MissionCategory.STUDY,
//                            MissionVisibility.ALL);
//            MissionCreateResponse missionCreateResponse =
//                    missionService.createMission(missionCreateRequest);
//
//            SecurityContextHolder.clearContext();
//            PrincipalDetails principal = new PrincipalDetails(2L, "USER");
//            Authentication authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            principal, "password", principal.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//
//            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
//            LocalDateTime missionRecordFinishedAt =
//                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
//            MissionRecordCreateRequest missionRecordCreateRequest =
//                    new MissionRecordCreateRequest(
//                            missionCreateResponse.missionId(),
//                            missionRecordStartedAt,
//                            missionRecordFinishedAt,
//                            32,
//                            14);
//            Long missionRecord =
//                    missionRecordService.createMissionRecord(missionRecordCreateRequest);
//            MissionRecordImageCreateRequest request =
//                    new MissionRecordImageCreateRequest(missionRecord, ImageFileExtension.JPEG);
//
//            // when, then
//            assertThatThrownBy(() -> imageService.createMissionRecordPresignedUrl(request))
//                    .isInstanceOf(CustomException.class)
//                    .hasMessage(ErrorCode.MISSION_RECORD_USER_MISMATCH.getMessage());
//        }

        @Test
        void 입력_값이_정상이라면_예외가_발생하지_않는다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(new Profile("testNickname", "testImageUrl")));
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
            Long missionRecord =
                    missionRecordService.createMissionRecord(missionRecordCreateRequest);
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(missionRecord, ImageFileExtension.JPEG);

            // when, then
            assertThatCode(() -> imageService.createMissionRecordPresignedUrl(request))
                    .doesNotThrowAnyException();
        }

        @Test
        void 입력_값이_정상이라면_PresignedUrl이_정상적으로_생성된다() {
            // given
            Member member =
                    memberRepository.save(
                            Member.createNormalMember(new Profile("testNickname", "testImageUrl")));
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
            Long missionRecordId =
                    missionRecordService.createMissionRecord(missionRecordCreateRequest);
            ImageFileExtension imageFileExtension = ImageFileExtension.JPEG;
            MissionRecordImageCreateRequest request =
                    new MissionRecordImageCreateRequest(missionRecordId, imageFileExtension);

            // when
            PresignedUrlResponse missionRecordPresignedUrl =
                    imageService.createMissionRecordPresignedUrl(request);

            // then
            assertThat(missionRecordPresignedUrl.presignedUrl())
                    .startsWith(
                            String.format(
                                    "https://kr.object.ncloudstorage.com/local/mission_record/%s/image.jpeg",
                                    missionRecordId));
        }
    }
}
