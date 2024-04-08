package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRelationRepository
        extends JpaRepository<MemberRelation, Long>, MemberRelationRepositoryCustom {
    Optional<MemberRelation> findBySourceIdAndTargetId(Long sourceId, Long targetId);

    boolean existsBySourceIdAndTargetId(Long sourceId, Long targetId);

    Long countBySourceId(Long sourceId);

    Long countByTargetId(Long targetId);

    List<MemberRelation> findAllBySourceId(Long memberId);

    List<MemberRelation> findAllBySourceIdAndTargetIn(Long sourceId, List<Member> targetIds);

    List<MemberRelation> findAllByTargetId(Long targetId);

    List<MemberRelation> findBySource(Member source);
}
