package com.depromeet.domain.ranking.dao;

import com.depromeet.domain.ranking.domain.Ranking;

public interface RankingRepositoryCustom {
    void saveOrUpdate(Ranking ranking);
}
