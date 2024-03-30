package com.depromeet.domain.comment.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String content;

    @org.hibernate.annotations.Comment("댓글을 작성한 회원")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_record_id")
    private MissionRecord missionRecord;

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(String content, Member member, MissionRecord missionRecord) {
        this.content = content;
        this.member = member;
        this.missionRecord = missionRecord;
    }

    public static Comment createComment(
            String content, Member member, MissionRecord missionRecord) {
        return Comment.builder()
                .content(content)
                .member(member)
                .missionRecord(missionRecord)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
