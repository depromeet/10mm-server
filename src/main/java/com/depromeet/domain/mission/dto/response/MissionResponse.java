package com.depromeet.domain.mission.dto.response;

import com.depromeet.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO: record 변환, record 변환 시 Schema 어노테이션 적용 가능 여부 체크, from으로 정적 팩토리 메서드, 단건 조회, 리스트 조회 별도 DTO 분리
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

    public MissionResponse(Mission mission) {
        this.missionId = mission.getId();
        this.name = mission.getName();
        this.content = mission.getContent();
        this.category = mission.getCategory().getValue();
        this.visibility = mission.getVisibility().getValue();
        this.sort = mission.getSort();
    }
}
