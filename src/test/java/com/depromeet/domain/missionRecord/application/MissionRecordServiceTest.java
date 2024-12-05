package com.depromeet.domain.missionRecord.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.domain.missionRecord.dto.response.MissionRecordFindOneResponse;
import com.depromeet.domain.missionRecord.dto.response.MissionStatisticsResponse;
import com.depromeet.domain.reaction.dao.ReactionRepository;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.util.SecurityUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionRecordServiceTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime missionStartedAt = LocalDateTime.of(2024, 1, 24, 22, 58, 53);
    @Autowired MissionRecordService missionRecordService;
    @Autowired MissionService missionService;
    @Autowired MemberRepository memberRepository;
    @Autowired MissionRepository missionRepository;
    @Autowired MissionRecordRepository missionRecordRepository;
    @Autowired ReactionRepository reactionRepository;
    @MockBean SecurityUtil securityUtil;
    private Member member;
    private Mission mission;

    @BeforeEach
    void setUp() {
        when(securityUtil.getCurrentMemberId()).thenReturn(1L);

        member =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test", "test", "test"), "test");
        memberRepository.save(member);
        mission =
                Mission.createMission(
                        "test",
                        "test",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        missionStartedAt,
                        missionStartedAt.plusWeeks(2),
                        LocalTime.of(21, 0),
                        member);
        missionRepository.save(mission);
    }

    @Test
    void 하루가_지나_미션_인증한_경우_에러를_발생시킨다() {
        // exception
        assertThrows(
                CustomException.class,
                () ->
                        missionRecordService.createMissionRecord(
                                new MissionRecordCreateRequest(
                                        mission.getId(), now, now.plusDays(1), 20, 0)));
    }

    @Test
    void 진행중인_미션기록을_삭제한다() {
        // given
        missionRecordService.createMissionRecord(
                new MissionRecordCreateRequest(mission.getId(), now, now.plusMinutes(10), 10, 0));

        // when
        missionRecordService.deleteInProgressMissionRecord();

        // then
        Long missionId = mission.getId();

        assertThrows(
                CustomException.class, () -> missionRecordService.findOneMissionRecord(missionId));
    }

    @Test
    void 리액션이_존재하는_미션기록을_삭제한다() {
        // given
        missionRecordService.createMissionRecord(
                new MissionRecordCreateRequest(mission.getId(), now, now.plusMinutes(10), 10, 0));

        Member reactedMember =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test", "test", "test"), "test");
        memberRepository.save(reactedMember);

        Reaction reaction =
                Reaction.createReaction(
                        EmojiType.BLUE_HEART,
                        reactedMember,
                        missionRecordRepository.findById(1L).get());
        reactionRepository.save(reaction);

        // when
        missionRecordService.deleteInProgressMissionRecord();

        // then
        Long missionId = mission.getId();

        assertThrows(
                CustomException.class, () -> missionRecordService.findOneMissionRecord(missionId));
    }

    @Test
    void 미션별_상세_통계_조회한다() {
        // given
        long durationMinute = 17;
        String defaultImage = "https://image.10mm.site/default.png";
        LocalDateTime recordStartedAt = LocalDateTime.of(2024, 1, 25, 22, 58, 53);
        LocalDateTime recordFinishedAt = recordStartedAt.plusMinutes(durationMinute);

        // 연속 성공 3일
        for (int i = 0; i < 3; i++) {
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            Duration.ofMinutes(durationMinute),
                            recordStartedAt.plusDays(i),
                            recordFinishedAt.plusDays(i),
                            mission);
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("testRemark " + i, defaultImage);
            missionRecordRepository.save(missionRecord);
        }

        // 연속 성공 4일
        for (int i = 4; i < 8; i++) {
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            Duration.ofMinutes(durationMinute),
                            recordStartedAt.plusDays(i),
                            recordFinishedAt.plusDays(i),
                            mission);
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("testRemark " + i, defaultImage);
            missionRecordRepository.save(missionRecord);
        }

        // when
        MissionStatisticsResponse missionStatistics =
                missionRecordService.findMissionStatistics(mission.getId());

        // then
        assertEquals(missionStatistics.totalMissionHour(), 1);
        assertEquals(missionStatistics.totalMissionMinute(), 59);
        assertEquals(missionStatistics.totalSymbolStack(), 7);
        assertEquals(missionStatistics.continuousSuccessDay(), 4);
        assertEquals(missionStatistics.totalSuccessDay(), 7);
        assertEquals(missionStatistics.totalMissionAttainRate(), 46.7);
        assertEquals(missionStatistics.timeTable().size(), 7);
    }

    @Nested
    class 미션기록_단건_조회할_때 {
        @Test
        void 미션이_생성된_당일에_미션기록을_완료한_경우라면_sinceDay가_1이된다() {
            // given
            LocalDateTime missionRecordStartedAt = missionStartedAt;
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            duration, missionRecordStartedAt, missionRecordFinishedAt, mission);
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("remark", "imageUrl");
            MissionRecord savedMissionRecord = missionRecordRepository.save(missionRecord);

            // when
            MissionRecordFindOneResponse queryMissionRecord =
                    missionRecordService.findOneMissionRecord(savedMissionRecord.getId());

            // then
            assertEquals(1, queryMissionRecord.sinceDay());
        }

        @Test
        void 미션이_생성되고_일주일후에_미션기록을_완료한_경우라면_sinceDay가_8이된다() {
            // given
            LocalDateTime missionRecordStartedAt = missionStartedAt.plusDays(7);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            duration, missionRecordStartedAt, missionRecordFinishedAt, mission);
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("remark", "imageUrl");
            MissionRecord savedMissionRecord = missionRecordRepository.save(missionRecord);

            // when
            MissionRecordFindOneResponse queryMissionRecord =
                    missionRecordService.findOneMissionRecord(savedMissionRecord.getId());

            // then
            assertEquals(8, queryMissionRecord.sinceDay());
        }
    }
}
