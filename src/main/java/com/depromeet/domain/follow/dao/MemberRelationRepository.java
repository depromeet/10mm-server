package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRelationRepository extends JpaRepository<MemberRelation, Long> {
    Optional<MemberRelation> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
