package com.depromeet.domain.missionRecord.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
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
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_record_id")
    private Long id;

    private Duration duration;

    @Comment("미션 일지")
    private String remark;

    @Comment("인증 사진")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ImageUploadStatus uploadStatus;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Builder(access = AccessLevel.PRIVATE)
    private MissionRecord(
            Duration duration,
            String remark,
            String imageUrl,
            ImageUploadStatus uploadStatus,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Mission mission) {
        this.duration = duration;
        this.remark = remark;
        this.uploadStatus = uploadStatus;
        this.imageUrl = imageUrl;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.mission = mission;
    }

    public static MissionRecord createMissionRecord(
            Duration duration, LocalDateTime startedAt, LocalDateTime finishedAt, Mission mission) {
        return MissionRecord.builder()
                .duration(duration)
                .uploadStatus(ImageUploadStatus.NONE)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .mission(mission)
                .build();
    }

    public void updateUploadStatusPending() {
        if (this.uploadStatus != ImageUploadStatus.NONE) {
            throw new CustomException(ErrorCode.MISSION_RECORD_UPLOAD_STATUS_IS_NOT_NONE);
        }
        this.uploadStatus = ImageUploadStatus.PENDING;
    }

    public void updateUploadStatusComplete(String remark, String imageUrl) {
        if (this.uploadStatus != ImageUploadStatus.PENDING) {
            throw new CustomException(ErrorCode.MISSION_RECORD_UPLOAD_STATUS_IS_NOT_PENDING);
        }
        this.uploadStatus = ImageUploadStatus.COMPLETE;
        this.remark = remark;
        this.imageUrl = imageUrl;
    }
}
