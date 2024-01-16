package com.depromeet.domain.missionRecord.dao;

import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
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
}
