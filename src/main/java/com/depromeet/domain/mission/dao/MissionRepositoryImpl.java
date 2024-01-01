package com.depromeet.domain.mission.dao;

import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Mission> findByMissionId(Long missionId) {
        Mission findMission =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .fetchJoin()
                        .where(mission.id.eq(missionId))
                        .fetchOne();
        return Optional.ofNullable(findMission);
    }

    @Override
    public Slice<Mission> findAllMission(
            Member member, Pageable pageable, Long lastId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .where(ltMissionId(lastId), memberIdEq(member.getId()))
                        .orderBy(mission.id.desc())
                        .limit(pageable.getPageSize() + 1);

        List<Mission> missions = query.fetch();

        return checkLastPage(pageable, missions);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId == null ? null : mission.member.id.eq(memberId);
    }

    private BooleanExpression ltMissionId(Long lastId) {
        return lastId == null ? null : mission.id.lt(lastId);
    }

    // 무한 스크롤 방식 처리하는 메서드
    private Slice<Mission> checkLastPage(
            Pageable pageable, List<Mission> result) {

        boolean hasNext = false;

        // 조회한 결과 개수가 요청한 페이지 사이즈보다 크면 뒤에 더 있음, next = true
        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
			result.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(result, pageable, hasNext);
    }
}
