package com.depromeet.domain.mission.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionTest {

    Member member;

    @BeforeEach
    void setUp() {
        Profile profile = Profile.createProfile("testNickname", "testProfileImageUrl");
        member = Member.createNormalMember(profile);
    }

    @Test
    void 미션_카테고리_기타값_테스트() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2023, 12, 15, 1, 37, 0);
        Mission mission =
                Mission.createMission(
                        "testMissionName",
                        "testMissionContent",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        startedAt,
                        finishedAt,
                        LocalTime.of(21, 0),
                        member);

        // when
        MissionCategory category = mission.getCategory();

        // then
        assertEquals(MissionCategory.ETC, category);
    }

    @Test
    void 미션_공개여부_공개_테스트() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2023, 12, 15, 1, 37, 0);
        Mission mission =
                Mission.createMission(
                        "testMissionName",
                        "testMissionContent",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        startedAt,
                        finishedAt,
                        LocalTime.of(21, 0),
                        member);

        // when
        MissionVisibility visibility = mission.getVisibility();

        // then
        assertEquals(MissionVisibility.ALL, visibility);
    }

    @Test
    void 미션_아카이빙_기본값_NONE_테스트() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2023, 12, 15, 1, 37, 0);
        Mission mission =
                Mission.createMission(
                        "testMissionName",
                        "testMissionContent",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        startedAt,
                        finishedAt,
                        LocalTime.of(21, 0),
                        member);

        // when
        ArchiveStatus archiveStatus = mission.getArchiveStatus();

        // then
        assertEquals(ArchiveStatus.NONE, archiveStatus);
    }
}
