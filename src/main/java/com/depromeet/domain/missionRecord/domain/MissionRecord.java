package com.depromeet.domain.missionRecord.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.mission.domain.Mission;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_record_id")
    private Long id;

    @Column private Integer duration;

    @Column(columnDefinition = "text not null COMMENT '미션 일지'")
    @Lob
    private String remark;

    @Enumerated(EnumType.STRING)
    private ImageUploadStatus uploadStatus;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Builder(access = AccessLevel.PRIVATE)
    private MissionRecord(
            Integer duration,
            String remark,
            ImageUploadStatus uploadStatus,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Mission mission) {
        this.duration = duration;
        this.remark = remark;
        this.uploadStatus = uploadStatus;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.mission = mission;
    }

    public static MissionRecord createMissionRecord(
            Integer duration,
            String remark,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Mission mission) {
        return MissionRecord.builder()
                .duration(duration)
                .remark(remark)
                .uploadStatus(ImageUploadStatus.NONE)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .mission(mission)
                .build();
    }
}
