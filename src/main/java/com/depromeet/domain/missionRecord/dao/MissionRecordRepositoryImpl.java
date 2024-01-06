package com.depromeet.domain.missionRecord.dao;

import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
                .where(missionIdEq(missionId), yearMonthEq(yearMonth))
                .orderBy(missionRecord.startedAt.asc())
                .fetch();
    }

    private BooleanExpression missionIdEq(Long missionId) {
        return missionRecord.mission.id.eq(missionId);
    }

    private BooleanExpression yearMonthEq(YearMonth yearMonth) {
        return missionRecord
                .createdAt
                .yearMonth()
                .eq(yearMonth.getYear() * 100 + yearMonth.getMonthValue());
    }
}
