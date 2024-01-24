package com.depromeet.domain.member.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Embedded private Profile profile = Profile.createProfile("", "");

    @Embedded private OauthInfo oauthInfo;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private MemberVisibility visibility;

    private LocalDateTime lastLoginAt;

    private String username;

    private String password;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Mission> missions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            Profile profile,
            OauthInfo oauthInfo,
            MemberStatus status,
            MemberRole role,
            MemberVisibility visibility,
            LocalDateTime lastLoginAt,
            String username,
            String password) {
        this.profile = profile;
        this.oauthInfo = oauthInfo;
        this.status = status;
        this.role = role;
        this.visibility = visibility;
        this.lastLoginAt = lastLoginAt;
        this.username = username;
        this.password = password;
    }

    public static Member createGuestMember(OauthInfo oauthInfo, String nickname) {
        return Member.builder()
                .profile(Profile.createProfile(nickname, null))
                .oauthInfo(oauthInfo)
                .status(MemberStatus.NORMAL)
                .role(MemberRole.GUEST)
                .visibility(MemberVisibility.PUBLIC)
                .build();
    }

    public static Member createGuestMember(String username, String password) {
        return Member.builder()
                .username(username)
                .password(password)
                .status(MemberStatus.NORMAL)
                .role(MemberRole.GUEST)
                .visibility(MemberVisibility.PUBLIC)
                .build();
    }

    public static Member createNormalMember(Profile profile) {
        return Member.builder()
                .profile(profile)
                .status(MemberStatus.NORMAL)
                .role(MemberRole.USER)
                .visibility(MemberVisibility.PUBLIC)
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void register(String nickname) {
        validateRegisterAvailable();
        // TODO: Profile 클래스를 제거하고 Member 클래스 필드로 변경
        // TODO: profileImageUrl이 항상 null이 되는 문제 해결
        // TODO: Profile.createProfile에서 url에 null이 아닌 this.profile.getProfileImageUrl()을 넣어야 함
        this.profile = Profile.createProfile(nickname, null);
        this.role = MemberRole.USER;
    }

    public void updateProfile(Profile profile) {
        this.profile = profile;
    }

    public void withdrawal() {
        if (this.status == MemberStatus.DELETED) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_DELETED);
        }
        this.status = MemberStatus.DELETED;
    }

    private void validateRegisterAvailable() {
        if (role != MemberRole.GUEST) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }
}
