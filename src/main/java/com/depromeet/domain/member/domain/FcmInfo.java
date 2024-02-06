package com.depromeet.domain.member.domain;

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

    public static FcmInfo createFcmInfo() {
        return FcmInfo.builder().appAlarm(true).build();
    }

    public static FcmInfo toggleAlarm(FcmInfo fcmState) {
        return new FcmInfo(fcmState.getFcmToken(), !fcmState.getAppAlarm());
    }

    public static FcmInfo disableAlarm(FcmInfo fcmInfo) {
        return new FcmInfo(fcmInfo.getFcmToken(), false);
    }

    public static FcmInfo deleteToken(FcmInfo fcmInfo) {
        return new FcmInfo("", fcmInfo.getAppAlarm());
    }

    public static FcmInfo updateToken(FcmInfo fcmState, String fcmToken) {
        return new FcmInfo(fcmToken, fcmState.getAppAlarm());
    }
}
