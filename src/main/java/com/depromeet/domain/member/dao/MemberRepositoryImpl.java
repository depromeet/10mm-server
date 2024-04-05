package com.depromeet.domain.member.dao;

import static com.depromeet.domain.member.domain.QMember.*;
import static com.depromeet.domain.mission.domain.QMission.*;
import static com.depromeet.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.DurationStatus;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
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
}
