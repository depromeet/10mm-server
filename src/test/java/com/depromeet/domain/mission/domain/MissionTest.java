package com.depromeet.domain.mission.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionTest {

    Member member;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        member = Member.createNormalMember(profile);
    }

    @Test
    void 미션_카테고리_기타값_테스트() {
        // given
        Mission mission =
                Mission.createPublicMission(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        member);

        // when
        MissionCategory category = mission.getCategory();

        // then
        assertEquals(MissionCategory.ETC, category);
    }

    @Test
    void 미션_공개여부_공개_테스트() {
        // given
        Mission mission =
                Mission.createPublicMission(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        member);

        // when
        MissionVisibility visibility = mission.getVisibility();

        // then
        assertEquals(MissionVisibility.ALL, visibility);
    }
}
