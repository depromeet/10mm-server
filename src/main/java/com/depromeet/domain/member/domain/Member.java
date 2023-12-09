package com.depromeet.domain.member.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
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

    @Builder
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
        return new Member(
                profile,
                MemberStatus.NORMAL,
                MemberRole.USER,
                MemberVisibility.PUBLIC,
                LocalDateTime.now());
    }
}
