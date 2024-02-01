package com.depromeet.domain.member.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Pattern(regexp = "[^0-9a-zA-Z가-힣 ]", message = "올바르지 않는 닉네임 표현입니다.")
    private String nickname;

    @Getter private String profileImageUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Profile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public static Profile createProfile(String nickname, String profileImageUrl) {
        return Profile.builder().nickname(nickname).profileImageUrl(profileImageUrl).build();
    }
}
