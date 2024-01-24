package com.depromeet.domain.follow.dao;

import static com.depromeet.domain.follow.domain.QMemberRelation.memberRelation;
import static com.depromeet.domain.member.domain.QMember.member;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.follow.domain.MemberRelation;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRelationRepositoryImpl implements MemberRelationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberRelation> findAllBySourceId(Long memberId) {
        return jpaQueryFactory
                .selectFrom(memberRelation)
                .leftJoin(memberRelation.target, member)
                .leftJoin(memberRelation.target.missions, mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .where(memberRelation.source.id.eq(memberId))
                .fetch();
    }

    private BooleanExpression sourceIdEq(Long sourceId) {
        return memberRelation.source.id.eq(sourceId);
    }

    //    @Override
    //    public List<Member> findFollowedMemberList(Long memberId) {
    //        List<Member> memberRelations = jpaQueryFactory
    //                .selectFrom(member)
    //                .join(memberRelation.follower)
    //
    //                .leftJoin(memberRelation.following, member)
    //                .fetchJoin()
    //                .leftJoin(memberRelation.following.missions, mission)
    //                .on(member.id.eq(mission.member.id))
    //                .leftJoin(mission.missionRecords, missionRecord)
    //                .on(mission.id.eq(missionRecord.mission.id))
    //                .where(memberRelation.follower.id.eq(memberId))
    //                .orderBy(orderByTest().desc())
    //                .fetch();
    //
    //        return memberRelations;
    //    }
    //
    //    private DateTimeExpression<LocalDateTime> orderByTest() {
    //        LocalDate now = LocalDate.now();
    //        return new CaseBuilder().when(
    //                memberRelation.following.id.isNotNull()
    //                        .and(Expressions.dateTemplate(LocalDate.class, "DATE_FORMAT({0},
    // %Y-%m-%d)",missionRecord.startedAt).eq(now))
    //                )
    //                .then(missionRecord.startedAt)
    //                .otherwise(memberRelation.createdAt);
    //    }
    //
    //    private BooleanExpression followerIdEq(Long memberId) {
    //        return memberId == null ? null : memberRelation.follower.id.eq(memberId);
    //    }
}
