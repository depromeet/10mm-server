package com.depromeet.domain.follow.dao;

import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.response.FollowCountsProjection;
import com.depromeet.domain.follow.dto.response.FollowStatusProjection;
import com.depromeet.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRelationRepository
        extends JpaRepository<MemberRelation, Long>, MemberRelationRepositoryCustom {
    Optional<MemberRelation> findBySourceIdAndTargetId(Long sourceId, Long targetId);

    boolean existsBySourceIdAndTargetId(Long sourceId, Long targetId);

    List<MemberRelation> findAllBySourceIdAndTargetIn(Long sourceId, List<Member> targetIds);

    @EntityGraph(attributePaths = "source")
    List<MemberRelation> findAllByTargetId(Long targetId);

    List<MemberRelation> findAllByTargetIdAndSourceIn(Long targetId, List<Member> sourceMembers);

    @Query(
            value =
                    """
                    select
                        (select count(*) from member_relation where source_id = :targetId) as followingCount,
                        (select count(*) from member_relation where target_id = :targetId) as followerCount
                    """,
            nativeQuery = true)
    FollowCountsProjection findFollowCounts(@Param("targetId") Long targetId);

    @Query(
            value =
                    """
                    select
                        exists(
                            select 1 from member_relation
                            where source_id = :viewerId and target_id = :targetId
                        ) as following,
                        exists(
                            select 1 from member_relation
                            where source_id = :targetId and target_id = :viewerId
                        ) as followedByMe
                    """,
            nativeQuery = true)
    FollowStatusProjection findFollowStatus(
            @Param("viewerId") Long viewerId, @Param("targetId") Long targetId);
}
