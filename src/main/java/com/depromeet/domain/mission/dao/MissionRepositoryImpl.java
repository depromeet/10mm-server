package com.depromeet.domain.mission.dao;

import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<MissionResponse> findByMissionId(Long missionId) {
        Mission findMission =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .fetchJoin()
                        .where(mission.id.eq(missionId))
                        .fetchOne();
        return Optional.ofNullable(findMission).map(MissionResponse::new);
    }

    @Override
    public Slice<MissionResponse> findMissionList(Member member, Pageable pageable, Long lastId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .where(ltMissionId(lastId), memberIdEq(member.getId()))
                        .orderBy(mission.id.desc())
                        .limit(pageable.getPageSize() + 1);

        List<Mission> missions = query.fetch();
        List<MissionResponse> list =
                missions.stream().map(MissionResponse::new).collect(Collectors.toList());
        return checkLastPage(pageable, list);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId == null ? null : mission.member.id.eq(memberId);
    }

    private BooleanExpression ltMissionId(Long lastId) {
        return lastId == null ? null : mission.id.lt(lastId);
    }

    // 무한 스크롤 방식 처리하는 메서드
    private Slice<MissionResponse> checkLastPage(
            Pageable pageable, List<MissionResponse> resultDtos) {

        boolean hasNext = false;

        // 조회한 결과 개수가 요청한 페이지 사이즈보다 크면 뒤에 더 있음, next = true
        if (resultDtos.size() > pageable.getPageSize()) {
            hasNext = true;
            resultDtos.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(resultDtos, pageable, hasNext);
    }
}
