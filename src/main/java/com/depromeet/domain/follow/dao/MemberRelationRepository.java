package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRelationRepository extends JpaRepository<MemberRelation, Long> {
    Optional<MemberRelation> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
