package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRelationRepository extends JpaRepository<MemberRelation, Long> {
    Optional<MemberRelation> findBySourceIdAndTargetId(Long sourceId, Long targetId);

    boolean existsBySourceIdAndTargetId(Long sourceId, Long targetId);

    Long countBySourceId(Long sourceId);

    Long countByTargetId(Long targetId);
}
