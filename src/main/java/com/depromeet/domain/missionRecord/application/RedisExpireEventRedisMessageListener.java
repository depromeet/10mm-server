package com.depromeet.domain.missionRecord.application;

import com.depromeet.global.common.constants.RedisExpireEventConstants;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisExpireEventRedisMessageListener implements MessageListener {
    private final MissionRecordExpirationService missionRecordExpirationService;
    private final Counter recoveryCounter;

    public RedisExpireEventRedisMessageListener(
            MissionRecordExpirationService missionRecordExpirationService,
            MeterRegistry meterRegistry) {
        this.missionRecordExpirationService = missionRecordExpirationService;
        this.recoveryCounter =
                Counter.builder("mission_record.expiration.recovered")
                        .tag("source", "redis_keyspace")
                        .register(meterRegistry);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String patternStr = new String(pattern);
        log.info(
                "RedisExpireEventRedisMessageListener.onMessage : message = {}, pattern = {}",
                message.toString(),
                patternStr);
        if (!patternStr.equals(RedisExpireEventConstants.REDIS_EXPIRE_EVENT_PATTERN.getValue())) {
            return;
        }
        String redisEntityName = message.toString().split(":")[0];
        if (!redisEntityName.equals("MissionRecordTtl")) {
            return;
        }
        Long missionRecordId = Long.parseLong(message.toString().split(":")[1]);
        if (missionRecordExpirationService.deleteExpiredRecord(missionRecordId) > 0) {
            recoveryCounter.increment();
        }
    }
}
