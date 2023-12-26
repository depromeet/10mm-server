package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MissionResponse {

    @Schema(description = "미션 ID")
    private final Long missionId;

    @Schema(description = "미션 이름")
    private final String name;

    @Schema(description = "미션 내용")
    private final String content;

    @Schema(description = "미션 카테고리")
    private final String category;

    @Schema(description = "미션 공개여부")
    private final String visibility;

    @Schema(description = "미션 정렬 값")
    private final Integer sort;

    public MissionResponse(
            Long missionId,
            String name,
            String content,
            MissionCategory category,
            MissionVisibility visibility,
            Integer sort) {
        this.missionId = missionId;
        this.name = name;
        this.content = content;
        this.category = category.getValue();
        this.visibility = visibility.getValue();
        this.sort = sort;
    }

    public MissionResponse(Mission mission) {
        this.missionId = mission.getId();
        this.name = mission.getName();
        this.content = mission.getContent();
        this.category = mission.getCategory().getValue();
        this.visibility = mission.getVisibility().getValue();
        this.sort = mission.getSort();
    }
}
