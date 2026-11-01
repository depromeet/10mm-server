package com.depromeet.domain.follow.dao;

import static com.depromeet.domain.follow.domain.QMemberRelation.memberRelation;
import static com.depromeet.domain.member.domain.QMember.member;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.response.FollowMemberPageRow;
import com.depromeet.domain.follow.dto.response.MemberFollowedResponse;
import com.depromeet.domain.follow.dto.response.MemberRelationPair;
import com.depromeet.domain.member.domain.QMember;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .where(sourceIdEq(memberId))
                .fetch();
    }

    @Override
    public List<MemberFollowedResponse> findAllFollowedMemberResponses(Long memberId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
        DateTimeExpression<LocalDateTime> latestCompletedAt = missionRecord.startedAt.max();
        NumberExpression<Integer> completedPriority =
                new CaseBuilder().when(latestCompletedAt.isNull()).then(1).otherwise(0);

        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                MemberFollowedResponse.class,
                                member.id,
                                member.profile.nickname,
                                member.profile.profileImageUrl))
                .from(memberRelation)
                .join(memberRelation.target, member)
                .leftJoin(member.missions, mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .on(
                        missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE),
                        missionRecord.startedAt.goe(startOfDay),
                        missionRecord.startedAt.lt(startOfNextDay))
                .where(sourceIdEq(memberId))
                .groupBy(
                        memberRelation.id,
                        memberRelation.createdAt,
                        member.id,
                        member.profile.nickname,
                        member.profile.profileImageUrl)
                .orderBy(
                        completedPriority.asc(),
                        latestCompletedAt.desc(),
                        memberRelation.createdAt.asc(),
                        memberRelation.id.asc())
                .fetch();
    }

    @Override
    public List<FollowMemberPageRow> findFollowingPage(
            Long memberId, LocalDateTime cursorCreatedAt, Long cursorRelationId, int limit) {
        return findRelationPage(
                memberRelation.target,
                memberRelation.source.id.eq(memberId),
                cursorCreatedAt,
                cursorRelationId,
                limit);
    }

    @Override
    public List<FollowMemberPageRow> findFollowerPage(
            Long memberId, LocalDateTime cursorCreatedAt, Long cursorRelationId, int limit) {
        return findRelationPage(
                memberRelation.source,
                memberRelation.target.id.eq(memberId),
                cursorCreatedAt,
                cursorRelationId,
                limit);
    }

    private List<FollowMemberPageRow> findRelationPage(
            QMember joinTarget,
            BooleanExpression ownerCondition,
            LocalDateTime cursorCreatedAt,
            Long cursorRelationId,
            int limit) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                FollowMemberPageRow.class,
                                memberRelation.id,
                                memberRelation.createdAt,
                                member))
                .from(memberRelation)
                .join(joinTarget, member)
                .where(ownerCondition, relationCursorCondition(cursorCreatedAt, cursorRelationId))
                .orderBy(memberRelation.createdAt.desc(), memberRelation.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<MemberRelationPair> findRelationsBetween(
            Long memberId, List<Long> targetMemberIds) {
        if (targetMemberIds.isEmpty()) {
            return List.of();
        }
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                MemberRelationPair.class,
                                memberRelation.source.id,
                                memberRelation.target.id))
                .from(memberRelation)
                .where(
                        memberRelation
                                .source
                                .id
                                .eq(memberId)
                                .and(memberRelation.target.id.in(targetMemberIds))
                                .or(
                                        memberRelation
                                                .target
                                                .id
                                                .eq(memberId)
                                                .and(memberRelation.source.id.in(targetMemberIds))))
                .fetch();
    }

    private BooleanExpression relationCursorCondition(
            LocalDateTime cursorCreatedAt, Long cursorRelationId) {
        if (cursorCreatedAt == null || cursorRelationId == null) {
            return null;
        }
        return memberRelation
                .createdAt
                .lt(cursorCreatedAt)
                .or(
                        memberRelation
                                .createdAt
                                .eq(cursorCreatedAt)
                                .and(memberRelation.id.lt(cursorRelationId)));
    }

    private BooleanExpression sourceIdEq(Long sourceId) {
        return memberRelation.source.id.eq(sourceId);
    }
}
