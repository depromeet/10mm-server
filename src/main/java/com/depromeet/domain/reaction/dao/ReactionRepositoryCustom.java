package com.depromeet.domain.reaction.dao;

import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import java.util.List;
import java.util.Map;

public interface ReactionRepositoryCustom {

    Map<EmojiType, List<Reaction>> findAllGroupByEmoji(Long missionRecordId);
}
