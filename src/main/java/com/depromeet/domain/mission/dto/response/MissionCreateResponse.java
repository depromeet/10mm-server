package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record MissionCreateResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 공개여부", defaultValue = "ALL") MissionVisibility visibility,
        @Schema(description = "미션 리마인드 알림 시간", defaultValue = "00:50:00", type = "string")
                LocalTime remindAt) {

    public static MissionCreateResponse from(Mission mission) {
        return new MissionCreateResponse(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory(),
                mission.getVisibility(),
                mission.getRemindAt());
    }
}
