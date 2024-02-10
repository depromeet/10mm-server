package com.depromeet.domain.reaction.dao;

import static com.depromeet.domain.reaction.domain.QReaction.reaction;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.types.Projections.*;

import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionRepositoryImpl implements ReactionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<EmojiType, List<Reaction>> findAllGroupByEmoji(Long missionRecordId) {
        return jpaQueryFactory
                .selectFrom(reaction)
                .innerJoin(reaction.member)
                .where(reaction.missionRecord.id.eq(missionRecordId))
                .fetchJoin()
                .transform(groupBy(reaction.emojiType).as(list(reaction)));
    }
}
