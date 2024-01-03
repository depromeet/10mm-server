package com.depromeet.domain.missionRecord.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MissionRecordTest {

    @Nested
    class 미션기록_생성_시 {
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        Member member = Member.createNormalMember(profile);
        LocalDateTime missionStartedAt = LocalDateTime.of(2023, 12, 1, 1, 5, 0);
        LocalDateTime missionFinishedAt = missionStartedAt.plusWeeks(2);
        Mission mission =
                Mission.createMission(
                        "testMissionName",
                        "testMissionContent",
                        1,
                        MissionCategory.ETC,
                        MissionVisibility.ALL,
                        missionStartedAt,
                        missionFinishedAt,
                        member);

        @Test
        void 업로드_상태_DEFAULT값은_NONE이다() {
            // given
            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            duration, missionRecordStartedAt, missionRecordFinishedAt, mission);

            // when
            ImageUploadStatus uploadStatus = missionRecord.getUploadStatus();

            // then
            assertEquals(ImageUploadStatus.NONE, uploadStatus);
        }

        @Test
        void 참여_시간은_초로_환산된다() {
            // given
            LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);
            MissionRecord missionRecord =
                    MissionRecord.createMissionRecord(
                            duration, missionRecordStartedAt, missionRecordFinishedAt, mission);

            // when, then
            assertEquals(missionRecord.getDuration(), (int) duration.getSeconds());
        }
    }
}
