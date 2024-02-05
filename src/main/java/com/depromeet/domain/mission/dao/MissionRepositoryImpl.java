package com.depromeet.domain.mission.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
                        .where(memberIdEq(memberId), visibilityByRelations(existsMemberRelations))
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
    public List<Mission> findFeedAll(List<Long> sourceIds) {
        return jpaQueryFactory
                .selectFrom(mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .fetchJoin()
                .where(
                        mission.member.id.in(sourceIds),
                        mission.visibility.in(MissionVisibility.FOLLOWER, MissionVisibility.ALL),
                        missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE))
                .orderBy(missionRecord.startedAt.desc())
                .fetch();
    }

    @Override
    public List<Mission> findFeedAllByMemberId(
            Long memberId, List<MissionVisibility> visibilities) {
        return jpaQueryFactory
                .selectFrom(mission)
                .leftJoin(mission.missionRecords, missionRecord)
                .fetchJoin()
                .where(
                        mission.member.id.eq(memberId),
                        mission.visibility.in(visibilities),
                        missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE))
                .orderBy(missionRecord.startedAt.desc())
                .fetch();
    }

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
}
