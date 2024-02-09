package com.depromeet.domain.reaction.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private EmojiType emojiType;

    @Comment("리액션을 추가한 회원")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_record_id")
    private MissionRecord missionRecord;

    @Builder(access = AccessLevel.PRIVATE)
    private Reaction(EmojiType emojiType, Member member, MissionRecord missionRecord) {
        this.emojiType = emojiType;
        this.member = member;
        this.missionRecord = missionRecord;
    }

    public static Reaction createReaction(
            EmojiType emojiType, Member member, MissionRecord missionRecord) {
        return Reaction.builder()
                .emojiType(emojiType)
                .member(member)
                .missionRecord(missionRecord)
                .build();
    }

    public void updateEmojiType(EmojiType emojiType) {
        this.emojiType = emojiType;
    }
}
