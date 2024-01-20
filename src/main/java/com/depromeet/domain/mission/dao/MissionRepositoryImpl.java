package com.depromeet.domain.mission.dao;

import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
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
    public Slice<Mission> findAllFinishedMission(Long memberId, int size, Long lastId) {
        JPAQuery<Mission> query =
                jpaQueryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionRecords, missionRecord)
                        .fetchJoin()
                        .where(
                                ltMissionId(lastId),
                                memberIdEq(memberId),
                                uploadStatusCompleteMissionEq(),
                                durationStatusFinishedEq())
                        .orderBy(mission.id.desc())
                        .limit((long) size + 1);

        List<Mission> missions = query.fetch();
        return checkLastPage(size, missions);
    }

    // 미션의 사용자 id 조건 검증 메서드
    private BooleanExpression memberIdEq(Long memberId) {
        return memberId == null ? null : mission.member.id.eq(memberId);
    }

    // lastId보다 작은 미션 id 찾는 조건 메서드 (lastId 가 있다면 마지막 요청)
    private BooleanExpression ltMissionId(Long lastId) {
        return lastId == null ? null : mission.id.lt(lastId);
    }

    // 종료 미션 검증
    private BooleanExpression uploadStatusCompleteMissionEq() {
        return missionRecord.uploadStatus.eq(ImageUploadStatus.COMPLETE);
    }

    private BooleanExpression durationStatusFinishedEq() {
        return mission.durationStatus.eq(DurationStatus.FINISHED);
    }

    // 무한 스크롤 방식 처리하는 메서드
    private Slice<Mission> checkLastPage(int size, List<Mission> result) {

        boolean hasNext = false;

        // 조회한 결과 개수가 요청한 페이지 사이즈보다 크면 뒤에 더 있음, next = true
        if (result.size() > size) {
            hasNext = true;
            result.remove(size);
        }
        Pageable pageable = Pageable.unpaged();
        return new SliceImpl<>(result, pageable, hasNext);
    }
}
