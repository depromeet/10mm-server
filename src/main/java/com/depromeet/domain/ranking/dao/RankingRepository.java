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
                MERGE INTO ranking r
                USING (SELECT :memberId AS member_id, :symbolStack AS symbol_stack FROM DUAL) s
                ON (r.member_id = s.member_id)
                WHEN MATCHED THEN
                  UPDATE SET r.symbol_stack = s.symbol_stack, r.updated_at = SYSDATE
                WHEN NOT MATCHED THEN
                  INSERT (member_id, symbol_stack, created_at) VALUES (s.member_id, s.symbol_stack, SYSDATE)
                """,
            nativeQuery = true)
    void updateSymbolStackAndMemberId(
            @Param("symbolStack") long symbolStack, @Param("memberId") Long memberId);
}
