package com.depromeet.domain.missionRecord.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionRecordTest {

    private static Mission mission;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        Member member = Member.createNormalMember(profile);
        mission = Mission.registerPublicMission("testMissionName", "testMissionContent", member);
    }

    @Test
    void 미션기록_업로드_상태_DEFAULT값은_NONE이다() {
        // given
        int duration = 32;
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusMinutes(duration);
        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        duration, "testMissionRecord_remark", startedAt, finishedAt, mission);

        // when
        ImageUploadStatus uploadStatus = missionRecord.getUploadStatus();

        // then
        assertEquals(ImageUploadStatus.NONE, uploadStatus);
    }
}
