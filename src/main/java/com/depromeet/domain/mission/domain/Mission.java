package com.depromeet.domain.mission.domain;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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
    @Column(nullable = false, length = 30)
    private String content;

    @Enumerated(EnumType.STRING)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    private MissionVisibility visibility;

    @Comment("미션 정렬값")
    @Column(nullable = false)
    private Integer sort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    private ArchiveStatus archiveStatus;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MissionRecord> missionRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Mission(
            String name,
            String content,
            MissionCategory category,
            MissionVisibility visibility,
            Integer sort,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            ArchiveStatus archiveStatus,
            Member member) {
        this.name = name;
        this.content = content;
        this.category = category;
        this.visibility = visibility;
        this.sort = sort;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.archiveStatus = archiveStatus;
        this.member = member;
    }

    public static Mission createMission(
            String name,
            String content,
            MissionCategory category,
            MissionVisibility visibility,
            Integer sort,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Member member) {
        return Mission.builder()
                .name(name)
                .content(content)
                .category(category)
                .visibility(visibility)
                .sort(sort)
                .member(member)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .archiveStatus(ArchiveStatus.NONE)
                .build();
    }
}
