package com.depromeet.domain.mission.dto;

import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MissionResponse {

    @Schema(description = "미션 ID")
    private Long missionId;

    @Schema(description = "미션 이름")
    private String name;

    @Schema(description = "미션 내용")
    private String content;

    @Schema(description = "미션 카테고리")
    private MissionCategory category;

    @Schema(description = "미션 공개여부")
    private MissionVisibility visibility;

    @Schema(description = "미션 정렬 값")
    private Integer sort;

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
        this.category = category;
        this.visibility = visibility;
        this.sort = sort;
    }
}
