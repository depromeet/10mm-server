package com.depromeet.domain.member.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {
    private String nickname;
    @Getter
    private String profileImageUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Profile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public static Profile createProfile(String nickname, String profileImageUrl) {
        return Profile.builder().nickname(nickname).profileImageUrl(profileImageUrl).build();
    }
}
