package com.depromeet.domain.mission.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    // 내 미션 목록
    @Override
    public List<Mission> findMissionsWithRecords(Long memberId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .where(memberIdEq(memberId))
                        .orderBy(mission.id.desc())
                        .fetchJoin();
        return query.fetch();
    }

    @Override
    public List<Mission> findInProgressMissionsWithRecords(Long memberId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .where(memberIdEq(memberId), durationStatusInProgress())
                        .orderBy(mission.id.desc())
                        .fetchJoin();
        return query.fetch();
    }

    // 친구 미션 목록
    @Override
    public List<Mission> findMissionsWithRecordsByRelations(
            Long memberId, boolean existsMemberRelations) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .where(
                                memberIdEq(memberId),
                                durationStatusInProgress(),
                                visibilityByRelations(existsMemberRelations))
                        .orderBy(mission.id.desc())
                        .fetchJoin();
        return query.fetch();
    }

    @Override
    public void updateFinishedDurationStatus(LocalDateTime today) {
        jpaQueryFactory
                .update(mission)
                .set(mission.durationStatus, DurationStatus.FINISHED)
                .where(
                        mission.finishedAt.loe(today),
                        mission.durationStatus.ne(DurationStatus.FINISHED))
                .execute();
    }

    @Override
    public List<Mission> findAllFinishedMission(Long memberId) {
        return jpaQueryFactory
                .selectFrom(mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .fetchJoin()
                .where(memberIdEq(memberId), durationStatusFinishedEq())
                .orderBy(mission.finishedAt.desc())
                .fetch();
    }

    @Override
    public List<Mission> findMissionsWithRecordsByDate(LocalDate date, Long memberId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .where(
                                memberIdEq(memberId),
                                Expressions.dateTemplate(
                                                LocalDate.class, "DATE({0})", mission.startedAt)
                                        .loe(date),
                                Expressions.dateTemplate(
                                                LocalDate.class, "DATE({0})", mission.finishedAt)
                                        .goe(date))
                        .fetchJoin();
        return query.fetch();
    }

    // 미션의 사용자 id 조건 검증 메서드
    private BooleanExpression memberIdEq(Long memberId) {
        return memberId == null ? null : mission.member.id.eq(memberId);
    }

    private BooleanExpression visibilityByRelations(boolean existsRelations) {
        return existsRelations
                ? mission.visibility.in(MissionVisibility.FOLLOWER, MissionVisibility.ALL)
                : mission.visibility.in(MissionVisibility.ALL);
    }

    private BooleanExpression durationStatusInProgress() {
        return mission.durationStatus.in(DurationStatus.IN_PROGRESS);
    }

    // lastId보다 작은 미션 id 찾는 조건 메서드 (lastId 가 있다면 마지막 요청)
    private BooleanExpression ltMissionId(Long lastId) {
        return lastId == null ? null : mission.id.lt(lastId);
    }

    private BooleanExpression durationStatusFinishedEq() {
        return mission.durationStatus.eq(DurationStatus.FINISHED);
    }
}
