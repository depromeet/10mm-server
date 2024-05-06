package com.depromeet.domain.ranking.dao;

import com.depromeet.domain.ranking.domain.Ranking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class RankingRepositoryImpl implements RankingRepositoryCustom {
    @PersistenceContext private EntityManager entityManager;

    public void saveOrUpdate(Ranking ranking) {
        entityManager.flush();
        entityManager.merge(ranking);
    }
}
