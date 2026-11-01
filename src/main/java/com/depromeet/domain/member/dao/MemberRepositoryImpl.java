package com.depromeet.domain.member.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
    public List<Member> findMissionNonCompletedMembers(LocalDateTime today) {
        LocalDateTime start = today.toLocalDate().atStartOfDay();
        LocalDateTime end = today.toLocalDate().atTime(23, 59, 59);
        return jpaQueryFactory
                .selectFrom(member)
                .leftJoin(member.missions, mission)
                .fetchJoin()
                .leftJoin(mission.missionRecords, missionRecord)
                .on(missionRecord.createdAt.between(start, end))
                .where(
                        missionRecord
                                .isNull()
                                .or(missionRecord.uploadStatus.ne(ImageUploadStatus.COMPLETE)),
                        mission.durationStatus.eq(DurationStatus.IN_PROGRESS),
                        member.fcmInfo.fcmToken.isNotNull(),
                        mission.startedAt.loe(today),
                        mission.finishedAt.goe(today))
                .fetch();
    }

    @Override
    public List<Member> nicknameSearchPage(
            String nickname,
            Long currentMemberId,
            Integer cursorPriority,
            String cursorNickname,
            Long cursorMemberId,
            int limit) {
        NumberExpression<Integer> exactMatchPriority =
                new CaseBuilder().when(member.profile.nickname.eq(nickname)).then(0).otherwise(1);

        return jpaQueryFactory
                .selectFrom(member)
                .where(
                        member.profile.nickname.contains(nickname),
                        member.id.ne(currentMemberId),
                        memberSearchCursorCondition(
                                exactMatchPriority, cursorPriority, cursorNickname, cursorMemberId))
                .orderBy(exactMatchPriority.asc(), member.profile.nickname.asc(), member.id.asc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression memberSearchCursorCondition(
            NumberExpression<Integer> exactMatchPriority,
            Integer cursorPriority,
            String cursorNickname,
            Long cursorMemberId) {
        if (cursorPriority == null || cursorNickname == null || cursorMemberId == null) {
            return null;
        }
        return exactMatchPriority
                .gt(cursorPriority)
                .or(
                        exactMatchPriority
                                .eq(cursorPriority)
                                .and(member.profile.nickname.gt(cursorNickname)))
                .or(
                        exactMatchPriority
                                .eq(cursorPriority)
                                .and(member.profile.nickname.eq(cursorNickname))
                                .and(member.id.gt(cursorMemberId)));
    }
}
