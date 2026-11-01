package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.response.FollowMemberPageRow;
import com.depromeet.domain.follow.dto.response.MemberFollowedResponse;
import com.depromeet.domain.follow.dto.response.MemberRelationPair;
import java.time.LocalDateTime;
import java.util.List;

public interface MemberRelationRepositoryCustom {
    List<MemberRelation> findAllBySourceId(Long memberId);

    List<MemberFollowedResponse> findAllFollowedMemberResponses(Long memberId);

    List<FollowMemberPageRow> findFollowingPage(
            Long memberId, LocalDateTime cursorCreatedAt, Long cursorRelationId, int limit);

    List<FollowMemberPageRow> findFollowerPage(
            Long memberId, LocalDateTime cursorCreatedAt, Long cursorRelationId, int limit);

    List<MemberRelationPair> findRelationsBetween(Long memberId, List<Long> targetMemberIds);
}
