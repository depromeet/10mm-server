package com.depromeet.domain.member.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmInfo {

	private String fcmToken;
	private Boolean appAlarm;

	@Builder(access = AccessLevel.PRIVATE)
	private FcmInfo(String fcmToken, Boolean appAlarm) {
		this.fcmToken = fcmToken;
		this.appAlarm = appAlarm;
	}

	public static FcmInfo toggleAlarm(FcmInfo fcmState) {
		return new FcmInfo(fcmState.getFcmToken(), !fcmState.getAppAlarm());
	}

	public static FcmInfo deleteToken() {
		return new FcmInfo("", false);
	}

	public static FcmInfo updateToken(FcmInfo fcmState, String fcmToken) {
		// TODO: 푸시 알림 Toggle 기획 시 fcmState.getAppAlarm()로 변경
		return new FcmInfo(fcmToken, true);
	}

}
