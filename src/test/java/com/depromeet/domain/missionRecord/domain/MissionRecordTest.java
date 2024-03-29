package com.depromeet.domain.missionRecord.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MissionRecordTest {

    @Nested
    class 미션기록_생성_시 {
        Profile profile = Profile.createProfile("testNickname", "testProfileImageUrl");
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
                        LocalTime.of(21, 0),
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

        @Nested
        class 미션기록의_이미지_업로드_상태를 {
            @Test
            void PENDING으로_변경할때_업로드_상태가_NONE이_아니라면_예외가_발생한다()
                    throws NoSuchFieldException, IllegalAccessException {
                // given
                LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
                LocalDateTime missionRecordFinishedAt =
                        missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
                Duration duration = Duration.ofMinutes(32).plusSeconds(14);
                MissionRecord missionRecord =
                        MissionRecord.createMissionRecord(
                                duration, missionRecordStartedAt, missionRecordFinishedAt, mission);

                Field uploadStatusField = MissionRecord.class.getDeclaredField("uploadStatus");
                uploadStatusField.setAccessible(true);
                uploadStatusField.set(missionRecord, ImageUploadStatus.PENDING);

                // when, then
                assertThatThrownBy(() -> missionRecord.updateUploadStatusPending())
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                ErrorCode.MISSION_RECORD_UPLOAD_STATUS_IS_NOT_NONE.getMessage());
            }

            @Test
            void COMPLETE로_변경할때_업로드_상태가_PENDING상태가_아니라면_예외가_발생한다()
                    throws NoSuchFieldException, IllegalAccessException {
                // given
                LocalDateTime missionRecordStartedAt = LocalDateTime.of(2023, 12, 15, 1, 5, 0);
                LocalDateTime missionRecordFinishedAt =
                        missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
                Duration duration = Duration.ofMinutes(32).plusSeconds(14);
                MissionRecord missionRecord =
                        MissionRecord.createMissionRecord(
                                duration, missionRecordStartedAt, missionRecordFinishedAt, mission);

                Field uploadStatusField = MissionRecord.class.getDeclaredField("uploadStatus");
                uploadStatusField.setAccessible(true);
                uploadStatusField.set(missionRecord, ImageUploadStatus.COMPLETE);

                // when, then
                assertThatThrownBy(
                                () ->
                                        missionRecord.updateUploadStatusComplete(
                                                "testRemark", "testImageUrl"))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                ErrorCode.MISSION_RECORD_UPLOAD_STATUS_IS_NOT_PENDING.getMessage());
            }
        }
    }
}
