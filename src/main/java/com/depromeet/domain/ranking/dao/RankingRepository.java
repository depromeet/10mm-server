package com.depromeet.domain.ranking.dao;

import com.depromeet.domain.ranking.domain.Ranking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    // 최대 50개의 랭킹을 조회한다.
    List<Ranking> findTop50ByOrderBySymbolStackDesc();
}
