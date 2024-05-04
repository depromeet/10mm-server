package com.depromeet.domain.ranking.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ranking extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("번개 스택")
    private Long symbolStack;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder(access = AccessLevel.PRIVATE)
    private Ranking(Long symbolStack, Member member) {
        this.symbolStack = symbolStack;
        this.member = member;
    }

    public static Ranking createRanking(Long symbolStack, Member member) {
        return Ranking.builder().symbolStack(symbolStack).member(member).build();
    }

    public void updateSymbolStack(Long symbolStack) {
        this.symbolStack = symbolStack;
    }
}
