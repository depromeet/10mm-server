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
                .fetchJoin()
                .leftJoin(memberRelation.target.missions, mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .where(sourceIdEq(memberId))
                .fetch();
    }

    private BooleanExpression sourceIdEq(Long sourceId) {
        return memberRelation.source.id.eq(sourceId);
    }
}
