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
import com.depromeet.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.util.SecurityUtil;
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
    @Autowired DatabaseCleaner databaseCleaner;
    @MockBean SecurityUtil securityUtil;

    private Member member;
    private Mission mission;
    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        when(securityUtil.getCurrentMemberId()).thenReturn(1L);

        member = Member.createGuestMember(OauthInfo.createOauthInfo("test", "test"));
        memberRepository.save(member);

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
}
