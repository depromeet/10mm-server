package com.depromeet.domain.mission.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Comment;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.domain.MissionRecord;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long id;

    @Comment("미션 이름")
    @Column(nullable = false, length = 20)
    private String name;

    @Comment("미션 내용")
    @Column(columnDefinition = "text", nullable = false)
    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    private MissionVisibility visibility;

    @Comment("미션 정렬값")
    @Column(name = "sort", nullable = false)
    private Integer sort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MissionRecord> missionRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Mission(
            String name,
            String content,
            MissionCategory category,
            MissionVisibility visibility,
            Integer sort,
            Member member) {
        this.name = name;
        this.content = content;
        this.category = category;
        this.visibility = visibility;
        this.sort = sort;
        this.member = member;
    }

    public static Mission createPublicMission(
            String name,
            String content,
            MissionCategory category,
            MissionVisibility visibility,
			Integer sort,
            Member member) {
        return Mission.builder()
                .name(name)
                .content(content)
                .category(category)
                .visibility(visibility)
                .sort(sort)
                .member(member)
                .build();
    }
}
