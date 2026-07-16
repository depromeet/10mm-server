package com.depromeet.domain.ranking.dao;

import com.depromeet.domain.ranking.domain.Ranking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    // 최대 50개의 랭킹을 조회한다.
    List<Ranking> findTop50ByOrderBySymbolStackDesc();

    @Modifying
    @Query(
            value =
                    """
                INSERT INTO ranking (member_id, symbol_stack, created_at)
                VALUES (:memberId, :symbolStack, CURRENT_TIMESTAMP)
                ON CONFLICT (member_id) DO UPDATE
                SET symbol_stack = EXCLUDED.symbol_stack, updated_at = CURRENT_TIMESTAMP
                """,
            nativeQuery = true)
    void updateSymbolStackAndMemberId(
            @Param("symbolStack") long symbolStack, @Param("memberId") Long memberId);
}
