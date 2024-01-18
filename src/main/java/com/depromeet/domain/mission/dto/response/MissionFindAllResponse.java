package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.ArchiveStatus;
import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MissionFindAllResponse(
        @Schema(description = "미션 ID", defaultValue = "1") Long missionId,
        @Schema(description = "미션 이름", defaultValue = "default name") String name,
        @Schema(description = "미션 내용", defaultValue = "default content") String content,
        @Schema(description = "미션 카테고리", defaultValue = "STUDY") MissionCategory category,
        @Schema(description = "미션 공개여부", defaultValue = "ALL") MissionVisibility visibility,
        @Schema(description = "미션 진행 여부", defaultValue = "IN_PROGRESS") DurationStatus durationStatus,
        @Schema(description = "미션 아카이빙 상태", defaultValue = "NONE") ArchiveStatus archiveStatus,
        @Schema(description = "미션 정렬 값", defaultValue = "1") Integer sort,
        @Schema(description = "미션 상태", defaultValue = "1") MissionStatus missionStatus,
        @Schema(description = "인증 TTL 종료 시간", defaultValue = "NONE") LocalDateTime ttlFinishedAt,
        @Schema(description = "인증필요인경우 recordId", defaultValue = "1") Long missionRecordId) {

    public static MissionFindAllResponse of(
            Mission mission,
            MissionStatus missionStatus,
            LocalDateTime ttlFinishedAt,
            Long missionRecordId) {
        return new MissionFindAllResponse(
                mission.getId(),
                mission.getName(),
                mission.getContent(),
                mission.getCategory(),
                mission.getVisibility(),
				mission.getDurationStatus(),
                mission.getArchiveStatus(),
                mission.getSort(),
                missionStatus,
                ttlFinishedAt,
                missionRecordId);
    }
}
