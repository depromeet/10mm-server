package com.depromeet.domain.reaction.dao;

import com.depromeet.domain.reaction.domain.Reaction;
import java.util.List;

public interface ReactionRepositoryCustom {

    List<Reaction> findAllByMissionRecordId(Long missionRecordId);
}
