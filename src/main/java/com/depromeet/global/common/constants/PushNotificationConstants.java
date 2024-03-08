package com.depromeet.global.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushNotificationConstants {
    public static final String PUSH_SERVICE_TITLE = "10MM";
    public static final String PUSH_SERVICE_CONTENT = "%s님이 회원님을 팔로우하기 시작했습니다🥳";
    public static final String PUSH_NON_COMPLETE_MISSION_SERVICE_CONTENT =
            "아직 오늘 미션을 완료하지 않았어요! 10분 동안 빠르게 완료해볼까요?";
    public static final String PUSH_URGING_TITLE = "누가 내 미션을 기다린대요";
    public static final String PUSH_URGING_CONTENT = "%s님이 %s 미션을 기다리고 있어요 🥺";
    public static final String PUSH_REACTION_TITLE = "누가 나를 응원했어요!";
    public static final String PUSH_REACTION_CONTENT = "%s님이 %s 미션을 이모지로 응원했어요!";
	public static final String PUSH_MISSION_REMIND_TITLE = "미션 리마인드";
	public static final String PUSH_MISSION_REMIND_CONTENT = "미션 시작한 지 10분이 지났네요! 끝내시겠어요?";
}
