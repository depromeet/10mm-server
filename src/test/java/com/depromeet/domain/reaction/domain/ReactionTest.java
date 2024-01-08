package com.depromeet.domain.reaction.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReactionTest {

    Member member;
    Mission mission;
    MissionRecord missionRecord;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        member = Member.createNormalMember(profile);

        LocalDateTime startedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime finishedAt = startedAt.plusWeeks(2);
        mission =
                Mission.createMission(
                        "testMissionName",
                        "testMissionContent",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        startedAt,
                        finishedAt,
                        member);
        Duration duration = Duration.between(startedAt, finishedAt);
        missionRecord = MissionRecord.createMissionRecord(duration, startedAt, finishedAt, mission);
    }

    @Test
    void 좋아요_반응_테스트() {
        // given
        Reaction reaction = Reaction.createReaction(ReactionType.STAR, member, missionRecord);

        // when
        ReactionType type = reaction.getType();

        // then
        assertEquals(type, ReactionType.STAR);
    }
}
