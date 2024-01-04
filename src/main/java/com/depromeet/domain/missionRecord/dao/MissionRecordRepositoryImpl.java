package com.depromeet.domain.missionRecord.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MissionRecordRepositoryImpl implements MissionRecordRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<MissionRecord> findAllByMissionId(Long missionId, String year, String month) {
		return jpaQueryFactory
			.selectFrom(missionRecord)
			.where(missionIdEq(missionId), yearEq(year), monthEq(month))
			.fetch();
	}

	private BooleanExpression missionIdEq(Long missionId) {
		return missionRecord.mission.id.eq(missionId);
	}

	private BooleanExpression yearEq(String year) {
		return missionRecord.createdAt.year().stringValue().eq(year);
	}

	private BooleanExpression monthEq(String month) {
		return missionRecord.createdAt.month().stringValue().eq(month);
	}
}
