package com.depromeet.domain.reaction.dao;

import static com.depromeet.domain.reaction.domain.QReaction.reaction;

import com.depromeet.domain.reaction.domain.Reaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionRepositoryImpl implements ReactionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Reaction> findAllByMissionRecordId(Long missionRecordId) {
        return jpaQueryFactory
                .selectFrom(reaction)
                .innerJoin(reaction.member)
                .where(reaction.missionRecord.id.eq(missionRecordId))
                .fetch();
    }
}
