package com.depromeet.domain.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionCategory {
    EXERCISE("운동"),
    STUDY("공부"),
    READING("글 읽기"),
    WRITING("글 쓰기"),
    WATCHING("영상 보기 / 팟캐스트 듣기"),
    ETC("기타");

    private final String value;
}
