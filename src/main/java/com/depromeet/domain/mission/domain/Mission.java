package com.depromeet.domain.mission.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
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
import java.time.LocalTime;
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
    @Column(length = 30)
    private String content;

    @Comment("미션 정렬값")
    @Column(nullable = false)
    private Integer sort;

    @Enumerated(EnumType.STRING)
    private DurationStatus durationStatus;

    @Enumerated(EnumType.STRING)
    private ArchiveStatus archiveStatus;

    @Enumerated(EnumType.STRING)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    private MissionVisibility visibility;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalTime remindAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MissionRecord> missionRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Mission(
            String name,
            String content,
            Integer sort,
            DurationStatus durationStatus,
            ArchiveStatus archiveStatus,
            MissionCategory category,
            MissionVisibility visibility,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalTime remindAt,
            Member member) {
        this.name = name;
        this.content = content;
        this.sort = sort;
        this.durationStatus = durationStatus;
        this.archiveStatus = archiveStatus;
        this.category = category;
        this.visibility = visibility;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.remindAt = remindAt;
        this.member = member;
    }

    public static Mission createMission(
            String name,
            String content,
            Integer sort,
            MissionCategory category,
            MissionVisibility visibility,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalTime remindAt,
            Member member) {
        return Mission.builder()
                .name(name)
                .content(content)
                .sort(sort)
                .durationStatus(DurationStatus.IN_PROGRESS)
                .archiveStatus(ArchiveStatus.NONE)
                .category(category)
                .visibility(visibility)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .remindAt(remindAt)
                .member(member)
                .build();
    }

    public void updateMission(
            String name, String content, MissionVisibility visibility, LocalTime remindAt) {
        this.name = name;
        this.content = content;
        this.visibility = visibility;
        this.remindAt = remindAt;
    }

    public boolean isCompletedMissionToday() {
        return this.getMissionRecords().stream()
                .filter(
                        record ->
                                record.getStartedAt()
                                                .toLocalDate()
                                                .equals(LocalDateTime.now().toLocalDate())
                                        && record.getUploadStatus() == ImageUploadStatus.COMPLETE)
                .findFirst()
                .isPresent();
    }

    public boolean isFinished() {
        if (this.getDurationStatus() == DurationStatus.FINISHED
                || this.archiveStatus == ArchiveStatus.ARCHIVED
                || this.getFinishedAt().isBefore(LocalDateTime.now())) {
            return true;
        }
        return false;
    }
}
