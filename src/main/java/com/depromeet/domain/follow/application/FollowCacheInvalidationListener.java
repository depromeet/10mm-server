package com.depromeet.domain.follow.application;

import com.depromeet.domain.follow.cache.FollowSummaryCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FollowCacheInvalidationListener {

    private final FollowSummaryCache followSummaryCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictChangedMemberSummaries(FollowRelationChangedEvent event) {
        followSummaryCache.evict(event.sourceMemberId());
        followSummaryCache.evict(event.targetMemberId());
    }
}
