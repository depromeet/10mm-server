package com.depromeet.domain.follow.cache;

import com.depromeet.domain.follow.dto.response.FollowSummary;
import com.depromeet.infra.config.redis.CacheRedisProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FollowSummaryCache {

    private static final String KEY_PREFIX = "follow:summary:v1:";
    private static final Duration BASE_TTL = Duration.ofSeconds(60);
    private static final int TTL_JITTER_SECONDS = 15;

    private final StringRedisTemplate redisTemplate;
    private final CacheRedisProperties cacheRedisProperties;
    private final Counter hitCounter;
    private final Counter missCounter;
    private final Counter errorCounter;
    private final Counter evictionErrorCounter;

    public FollowSummaryCache(
            @Qualifier("cacheStringRedisTemplate") StringRedisTemplate redisTemplate,
            CacheRedisProperties cacheRedisProperties,
            MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.cacheRedisProperties = cacheRedisProperties;
        this.hitCounter = cacheCounter(meterRegistry, "hit");
        this.missCounter = cacheCounter(meterRegistry, "miss");
        this.errorCounter = cacheCounter(meterRegistry, "error");
        this.evictionErrorCounter =
                Counter.builder("cache.eviction.errors")
                        .tag("cache", "follow_summary")
                        .register(meterRegistry);
    }

    public Optional<FollowSummary> get(Long memberId) {
        if (!cacheRedisProperties.enabled()) {
            return Optional.empty();
        }
        try {
            String value = redisTemplate.opsForValue().get(key(memberId));
            if (value == null) {
                missCounter.increment();
                return Optional.empty();
            }
            String[] counts = value.split(":", 2);
            FollowSummary summary =
                    new FollowSummary(Long.parseLong(counts[0]), Long.parseLong(counts[1]));
            hitCounter.increment();
            return Optional.of(summary);
        } catch (RuntimeException exception) {
            errorCounter.increment();
            log.warn("Failed to read follow summary cache. memberId={}", memberId, exception);
            return Optional.empty();
        }
    }

    public void put(Long memberId, FollowSummary summary) {
        if (!cacheRedisProperties.enabled()) {
            return;
        }
        try {
            Duration ttl =
                    BASE_TTL.plusSeconds(
                            ThreadLocalRandom.current().nextInt(TTL_JITTER_SECONDS + 1));
            redisTemplate
                    .opsForValue()
                    .set(
                            key(memberId),
                            summary.followingCount() + ":" + summary.followerCount(),
                            ttl);
        } catch (RuntimeException exception) {
            errorCounter.increment();
            log.warn("Failed to write follow summary cache. memberId={}", memberId, exception);
        }
    }

    public void evict(Long memberId) {
        if (!cacheRedisProperties.enabled()) {
            return;
        }
        try {
            redisTemplate.delete(key(memberId));
        } catch (RuntimeException exception) {
            evictionErrorCounter.increment();
            log.warn("Failed to evict follow summary cache. memberId={}", memberId, exception);
        }
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }

    private Counter cacheCounter(MeterRegistry meterRegistry, String result) {
        return Counter.builder("cache.requests")
                .tag("cache", "follow_summary")
                .tag("result", result)
                .register(meterRegistry);
    }
}
