package com.depromeet.domain.missionRecord.application;

import static org.mockito.Mockito.when;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.util.SecurityUtil;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MissionRecordServiceTest {

    @Autowired MissionRecordService missionRecordService;
    @Autowired MissionService missionService;
    @Autowired DatabaseCleaner databaseCleaner;
    @MockBean SecurityUtil securityUtil;
    @Autowired EntityManager entityManager;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        when(securityUtil.getCurrentMemberId()).thenReturn(1L);
    }

    @Test
    void 진행중인_미션기록을_삭제한다() {
        // given
        Member member = Member.createGuestMember(OauthInfo.createOauthInfo("test", "test"));
        entityManager.persist(member);

        LocalDateTime now = LocalDateTime.now();
        Mission mission =
                Mission.createMission(
                        "test",
                        "test",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        now,
                        now.plusWeeks(2),
                        member);
        entityManager.persist(mission);

        missionRecordService.createMissionRecord(
                new MissionRecordCreateRequest(mission.getId(), now, now.plusMinutes(10), 10, 0));

        entityManager.flush();
        entityManager.clear();

        // when
        missionRecordService.deleteInProgressMissionRecord();
        entityManager.flush();
        entityManager.clear();

        // then
        Long missionId = mission.getId();

        Assertions.assertThrows(
                CustomException.class, () -> missionRecordService.findOneMissionRecord(missionId));
    }
}
