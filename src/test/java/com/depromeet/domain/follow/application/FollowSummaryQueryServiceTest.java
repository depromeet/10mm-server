package com.depromeet.domain.follow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.depromeet.domain.follow.cache.FollowSummaryCache;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.dto.response.FollowCountsProjection;
import com.depromeet.domain.follow.dto.response.FollowSummary;
import com.depromeet.infra.config.redis.CacheRedisProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

class FollowSummaryQueryServiceTest {

    @Test
    void Redis_장애_시_DB_집계로_fallback한다() {
        // given
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.opsForValue())
                .thenThrow(new RedisConnectionFailureException("cache unavailable"));
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        FollowSummaryCache cache =
                new FollowSummaryCache(
                        redisTemplate,
                        new CacheRedisProperties("localhost", 6380, null, true),
                        meterRegistry);

        MemberRelationRepository repository = mock(MemberRelationRepository.class);
        FollowCountsProjection counts = mock(FollowCountsProjection.class);
        when(counts.getFollowingCount()).thenReturn(3L);
        when(counts.getFollowerCount()).thenReturn(7L);
        when(repository.findFollowCounts(1L)).thenReturn(counts);
        FollowSummaryQueryService service =
                new FollowSummaryQueryService(repository, cache, meterRegistry);

        // when
        FollowSummary summary = service.find(1L);

        // then
        assertEquals(new FollowSummary(3L, 7L), summary);
        verify(repository).findFollowCounts(1L);
        assertEquals(
                1.0,
                meterRegistry
                        .get("cache.fallbacks")
                        .tag("cache", "follow_summary")
                        .counter()
                        .count());
        assertEquals(
                2.0,
                meterRegistry
                        .get("cache.requests")
                        .tag("cache", "follow_summary")
                        .tag("result", "error")
                        .counter()
                        .count());
    }

    @Test
    void 관계_변경_커밋_후_source와_target_캐시를_모두_제거한다() {
        // given
        FollowSummaryCache cache = mock(FollowSummaryCache.class);
        FollowCacheInvalidationListener listener = new FollowCacheInvalidationListener(cache);

        // when
        listener.evictChangedMemberSummaries(new FollowRelationChangedEvent(1L, 2L));

        // then
        verify(cache).evict(1L);
        verify(cache).evict(2L);
    }
}
