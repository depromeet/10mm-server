package com.depromeet.domain.member.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.mission.domain.Mission;

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

    @Embedded private Profile profile;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private MemberVisibility visibility;

    private LocalDateTime lastLoginAt;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private final List<Mission> missions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            Profile profile,
            MemberStatus status,
            MemberRole role,
            MemberVisibility visibility,
            LocalDateTime lastLoginAt) {
        this.profile = profile;
        this.status = status;
        this.role = role;
        this.visibility = visibility;
        this.lastLoginAt = lastLoginAt;
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
}
