package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import java.util.List;

public interface MemberRelationRepositoryCustom {
    List<MemberRelation> findAllBySourceId(Long memberId);
}
