package com.depromeet.domain.missionRecord.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRecordRepositoryImpl implements MissionRecordRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private static final long FEED_TAB_LIMIT = 100;

    @Override
    public List<MissionRecord> findAllByMissionIdAndYearMonth(Long missionId, YearMonth yearMonth) {
        return jpaQueryFactory
                .selectFrom(missionRecord)
                .where(
                        missionIdEq(missionId),
                        yearEq(yearMonth.getYear()),
                        monthEq(yearMonth.getMonthValue()))
                .orderBy(missionRecord.startedAt.asc())
                .fetch();
    }

    @Override
    public List<MissionRecord> findAllByCompletedMission(Long missionId) {
        return jpaQueryFactory
                .selectFrom(missionRecord)
                .where(missionIdEq(missionId), uploadStatusCompleteEq())
                .orderBy(missionRecord.startedAt.asc())
                .fetch();
    }

    @Override
    public boolean isCompletedMissionExistsToday(Long missionId) {
        LocalDate now = LocalDate.now();
        MissionRecord missionRecordFetchOne =
                jpaQueryFactory
                        .selectFrom(missionRecord)
                        .where(
                                missionIdEq(missionId),
                                yearEq(now.getYear()),
                                monthEq(now.getMonthValue()),
                                dayEq(now.getDayOfMonth()))
                        .fetchFirst();
        return missionRecordFetchOne != null;
    }

    @Override
    public List<FeedOneResponse> findFeedAll(List<Member> members) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                FeedOneResponse.class,
                                member.id,
                                member.profile.nickname,
                                member.profile.profileImageUrl,
                                mission.id,
                                mission.name,
                                missionRecord.id,
                                missionRecord.remark,
                                missionRecord.imageUrl,
                                missionRecord.duration,
                                mission.startedAt,
                                missionRecord.startedAt,
                                mission.finishedAt))
                .from(missionRecord)
                .leftJoin(missionRecord.mission, mission)
                .on(mission.id.eq(missionRecord.mission.id))
                .leftJoin(mission.member, member)
                .on(mission.member.id.eq(missionRecord.mission.member.id))
                .where(
                        missionRecord.mission.member.in(members),
                        missionRecord.mission.visibility.in(
                                MissionVisibility.FOLLOWER, MissionVisibility.ALL),
                        missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE))
                .orderBy(missionRecord.finishedAt.desc())
                .limit(FEED_TAB_LIMIT)
                .fetch();
    }

    @Override
    public List<MissionRecord> findFeedAllByMemberId(
            Long memberId, List<MissionVisibility> visibilities) {
        return jpaQueryFactory
                .selectFrom(missionRecord)
                .leftJoin(missionRecord.mission, mission)
                .fetchJoin()
                .where(
                        mission.visibility.in(visibilities),
                        mission.member.id.eq(memberId),
                        missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE))
                .orderBy(missionRecord.startedAt.desc())
                .fetch();
    }

    @Override
    public void deleteByMissionRecordId(Long missionRecordId) {
        jpaQueryFactory.delete(missionRecord).where(missionRecord.id.eq(missionRecordId)).execute();
    }

    private BooleanExpression missionIdEq(Long missionId) {
        return missionRecord.mission.id.eq(missionId);
    }

    private BooleanExpression yearEq(int year) {
        return missionRecord.startedAt.year().eq(year);
    }

    private BooleanExpression monthEq(int month) {
        return missionRecord.startedAt.month().eq(month);
    }

    private BooleanExpression dayEq(int day) {
        return missionRecord.startedAt.dayOfMonth().eq(day);
    }

    private BooleanExpression uploadStatusCompleteEq() {
        return missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE);
    }
}
