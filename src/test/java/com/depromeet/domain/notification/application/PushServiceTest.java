package com.depromeet.domain.notification.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.FcmInfo;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import com.depromeet.domain.notification.dto.request.PushUrgingSendRequest;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import com.depromeet.global.util.MemberUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PushServiceTest {
    @Autowired private DatabaseCleaner databaseCleaner;

    @Autowired private MemberUtil memberUtil;

    @MockBean private FcmService fcmService;

    @Autowired private MissionRepository missionRepository;

    @Autowired private NotificationRepository notificationRepository;

    @Autowired private MemberRepository memberRepository;

    @Autowired private MissionRecordRepository missionRecordRepository;

    @Autowired private PushService pushService;

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
    class 친구에게_미션을_재촉할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);

            // when, then
            assertThatThrownBy(() -> pushService.sendUrgingPush(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 미션이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when, then
            assertThatThrownBy(() -> pushService.sendUrgingPush(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MISSION_NOT_FOUND.getMessage());
        }

        @Test
        void 본인에게_재촉할_경우_예외를_발생시킨다() {
            // given
            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime missionStartedAt = today;
            LocalDateTime missionFinishedAt = today.plusWeeks(2);
            missionRepository.save(
                    Mission.createMission(
                            "testMissionName",
                            "testMissionContent",
                            1,
                            MissionCategory.ETC,
                            MissionVisibility.ALL,
                            missionStartedAt,
                            missionFinishedAt,
                            currentMember));

            // when, then
            assertThatThrownBy(() -> pushService.sendUrgingPush(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SELF_SENDING_NOT_ALLOWED.getMessage());
        }

        @Test
        void 종료된_미션을_재촉할_경우_예외를_발생시킨다() {
            // given
            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime missionStartedAt = today.minusWeeks(3);
            LocalDateTime missionFinishedAt = missionStartedAt.plusWeeks(2);
            missionRepository.save(
                    Mission.createMission(
                            "testMissionName",
                            "testMissionContent",
                            1,
                            MissionCategory.ETC,
                            MissionVisibility.ALL,
                            missionStartedAt,
                            missionFinishedAt,
                            targetMember));

            // when, then
            assertThatThrownBy(() -> pushService.sendUrgingPush(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FINISHED_MISSION_URGING_NOT_ALLOWED.getMessage());
        }

        @Test
        void 미션이_당일_완료된_경우_예외를_발생시킨다() {
            // given
            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));

            LocalDateTime today = LocalDateTime.now();
            LocalDateTime missionStartedAt = today;
            LocalDateTime missionFinishedAt = today.plusWeeks(2);
            Mission mission =
                    missionRepository.save(
                            Mission.createMission(
                                    "testMissionName",
                                    "testMissionContent",
                                    1,
                                    MissionCategory.ETC,
                                    MissionVisibility.ALL,
                                    missionStartedAt,
                                    missionFinishedAt,
                                    targetMember));

            LocalDateTime missionRecordStartedAt = today;
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);
            MissionRecord missionRecord =
                    missionRecordRepository.save(
                            MissionRecord.createMissionRecord(
                                    duration,
                                    missionRecordStartedAt,
                                    missionRecordFinishedAt,
                                    mission));
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("remark", "imageUrl");
            missionRecordRepository.save(missionRecord);

            // when, then
            assertThatThrownBy(() -> pushService.sendUrgingPush(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.TODAY_COMPLETED_MISSION_SENDING_NOT_ALLOWED.getMessage());
        }

        @Test
        void 정상적이라면_재촉하기_푸시메세지가_발송되고_히스토리가_저장된다() {
            // given
            when(fcmService.sendMessageSync(any(), any(), any())).thenReturn(null);

            PushUrgingSendRequest request = new PushUrgingSendRequest(1L);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            targetMember.updateFcmToken(FcmInfo.createFcmInfo(), "testFcmToken");
            memberRepository.save(targetMember);

            LocalDateTime today = LocalDateTime.now();
            LocalDateTime missionStartedAt = today.minusDays(1);
            LocalDateTime missionFinishedAt = today.plusWeeks(2);
            missionRepository.save(
                    Mission.createMission(
                            "testMissionName",
                            "testMissionContent",
                            1,
                            MissionCategory.ETC,
                            MissionVisibility.ALL,
                            missionStartedAt,
                            missionFinishedAt,
                            targetMember));

            // when
            pushService.sendUrgingPush(request);

            // then
            Optional<Notification> optionalNotification = notificationRepository.findById(1L);
            assertTrue(optionalNotification.isPresent());
            assertEquals(1, notificationRepository.findAll().size());
            assertEquals(
                    NotificationType.MISSION_URGING,
                    optionalNotification.get().getNotificationType());
        }
    }
}
