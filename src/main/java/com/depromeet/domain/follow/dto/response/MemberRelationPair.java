package com.depromeet.domain.follow.dto.response;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record MemberRelationPair(Long sourceId, Long targetId) {

    /** viewer가 팔로우 중인 회원 ID */
    public static Set<Long> followingIds(List<MemberRelationPair> relations, Long viewerId) {
        return relations.stream()
                .filter(relation -> relation.sourceId().equals(viewerId))
                .map(MemberRelationPair::targetId)
                .collect(Collectors.toSet());
    }

    /** viewer를 팔로우 중인 회원 ID */
    public static Set<Long> followerIds(List<MemberRelationPair> relations, Long viewerId) {
        return relations.stream()
                .filter(relation -> relation.targetId().equals(viewerId))
                .map(MemberRelationPair::sourceId)
                .collect(Collectors.toSet());
    }
}
