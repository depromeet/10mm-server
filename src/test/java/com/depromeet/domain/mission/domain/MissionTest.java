package com.depromeet.domain.mission.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionTest {

    private static Member member;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        member = Member.createNormalMember(profile);
    }

    @Test
    void 미션_카테고리_DEFAULT값은_기타이다() {
        // given
        Mission mission =
                Mission.registerPublicMission("testMissionName", "testMissionContent", member);

        // when
        MissionCategory category = mission.getCategory();

        // then
        assertEquals(MissionCategory.ETC, category);
    }

    @Test
    void 미션_공개여부_DEFAULT값은_공개이다() {
        // given
        Mission mission =
                Mission.registerPublicMission("testMissionName", "testMissionContent", member);

        // when
        MissionVisibility visibility = mission.getVisibility();

        // then
        assertEquals(MissionVisibility.PUBLIC, visibility);
    }
}
