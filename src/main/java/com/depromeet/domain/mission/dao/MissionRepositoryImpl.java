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

    /*
    1. 미션 레코드를 기준으로 한다. 그런데 여기에 미션 정보도 필요하고, 멤버 정보도 필요한 거임.
    2. 그래서 미션 레코드를 기준으로 쿼리하되 미션을 조인하고, 멤버도 조인한다.
    3. where 조건 필터링할 때 멤버 조인했으니까, 이 멤버가 sourceID 기반으로 한 멤버 리스트 안에 있는지 필터링만 하면 끝
    4. 정렬은 레코드 생성순서로
    */

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
