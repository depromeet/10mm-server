package com.depromeet.domain.follow.application;

import com.depromeet.domain.follow.cache.FollowSummaryCache;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.dto.response.FollowSummary;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class FollowSummaryQueryService {

    private final MemberRelationRepository memberRelationRepository;
    private final FollowSummaryCache followSummaryCache;
    private final Counter fallbackCounter;

    public FollowSummaryQueryService(
            MemberRelationRepository memberRelationRepository,
            FollowSummaryCache followSummaryCache,
            MeterRegistry meterRegistry) {
        this.memberRelationRepository = memberRelationRepository;
        this.followSummaryCache = followSummaryCache;
        this.fallbackCounter =
                Counter.builder("cache.fallbacks")
                        .tag("cache", "follow_summary")
                        .register(meterRegistry);
    }

    public FollowSummary find(Long memberId) {
        return followSummaryCache
                .get(memberId)
                .orElseGet(
                        () -> {
                            fallbackCounter.increment();
                            FollowSummary summary =
                                    FollowSummary.from(
                                            memberRelationRepository.findFollowCounts(memberId));
                            followSummaryCache.put(memberId, summary);
                            return summary;
                        });
    }
}
