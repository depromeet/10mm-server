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
                    "INSERT INTO Ranking (member_id, symbol_stack) "
                            + "VALUES (:memberId, :symbolStack) "
                            + "ON DUPLICATE KEY UPDATE member_id = :memberId, symbol_stack = :symbolStack",
            nativeQuery = true)
    void updateSymbolStackAndMemberId(
            @Param("memberId") Long memberId, @Param("symbolStack") long symbolStack);
}
