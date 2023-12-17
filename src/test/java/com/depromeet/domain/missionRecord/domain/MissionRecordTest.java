package com.depromeet.domain.missionRecord.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionRecordTest {

    Mission mission;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        Member member = Member.createNormalMember(profile);
        mission =
                Mission.createPublicMission(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.ETC,
                        MissionVisibility.PUBLIC,
                        member);
    }

    @Test
    void 미션기록_업로드_상태_DEFAULT값은_NONE이다() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2023, 12, 15, 1, 37, 0);
        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        32, "testMissionRecordRemark", startedAt, finishedAt, mission);

        // when
        ImageUploadStatus uploadStatus = missionRecord.getUploadStatus();

        // then
        assertEquals(ImageUploadStatus.NONE, uploadStatus);
    }
}