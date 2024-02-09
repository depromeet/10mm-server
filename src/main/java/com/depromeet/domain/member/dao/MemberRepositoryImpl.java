package com.depromeet.domain.member.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Member> findNonCompleteMissions(LocalDateTime now) {
        return jpaQueryFactory
                .selectFrom(member)
                .leftJoin(member.missions, mission)
                .fetchJoin()
                .leftJoin(mission.missionRecords, missionRecord)
                .on(missionRecord.id.eq(mission.id))
                .where(
                        mission.missionRecords.isEmpty(),
                        mission.startedAt.loe(now),
                        mission.finishedAt.goe(now))
                .fetch();
    }
}
