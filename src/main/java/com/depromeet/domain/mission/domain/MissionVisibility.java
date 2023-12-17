package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionVisibility {
    ALL("전체 공개"),
	FOLLOWER("팔로워에게 공개"),
    NONE("비공개");

    private final String value;
}
