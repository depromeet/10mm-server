package com.depromeet.domain.follow.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import com.depromeet.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "member_relation_uk",
                    columnNames = {"source_id", "target_id"})
        })
public class MemberRelation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_relation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Member source;

    @ManyToOne
    @JoinColumn(name = "target_id")
    private Member target;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberRelation(Long id, Member source, Member target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    public static MemberRelation createMemberRelation(Member source, Member target) {
        return MemberRelation.builder().source(source).target(target).build();
    }
}
