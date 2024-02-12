package com.depromeet.domain.missionRecord.application;

import static org.mockito.Mockito.when;

import com.depromeet.DatabaseCleaner;
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
import com.depromeet.domain.missionRecord.dto.response.MissionStatisticsResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.util.SecurityUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionRecordServiceTest {

    @Autowired MissionRecordService missionRecordService;
    @Autowired MissionService missionService;
    @Autowired MemberRepository memberRepository;
    @Autowired MissionRepository missionRepository;
    @Autowired MissionRecordRepository missionRecordRepository;
    @Autowired DatabaseCleaner databaseCleaner;
    @MockBean SecurityUtil securityUtil;

    private Member member;
    private Mission mission;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime missionStartedAt = LocalDateTime.of(2024, 1, 24, 22, 58, 53);

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
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
                        member);
        missionRepository.save(mission);
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

        Assertions.assertThrows(
                CustomException.class, () -> missionRecordService.findOneMissionRecord(missionId));
    }

    @Test
    void 미션별_상세_통계_조회한다() {
        // given
        long durationMinute = 17;
        String defaultImage = "https://image.10mm.today/default.png";
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
        Assertions.assertEquals(missionStatistics.totalMissionHour(), 1);
        Assertions.assertEquals(missionStatistics.totalMissionMinute(), 59);
        Assertions.assertEquals(missionStatistics.totalSymbolStack(), 7);
        Assertions.assertEquals(missionStatistics.continuousSuccessDay(), 4);
        Assertions.assertEquals(missionStatistics.totalSuccessDay(), 7);
        Assertions.assertEquals(missionStatistics.totalMissionAttainRate(), 46.7);
        Assertions.assertEquals(missionStatistics.timeTable().size(), 7);
    }
}
