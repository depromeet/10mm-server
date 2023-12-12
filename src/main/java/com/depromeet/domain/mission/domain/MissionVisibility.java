package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionVisibility {
	PUBLIC("공개"),
	PRIVATE("비공개");

	private final String value;
}
