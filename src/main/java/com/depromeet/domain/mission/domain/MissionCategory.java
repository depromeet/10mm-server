package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionCategory {
	EXERCISE("운동"),
	STUDY("공부"),
	READING("글읽기"),
	WRITING("글쓰기"),
	ETC("기타");

	private final String value;
}
