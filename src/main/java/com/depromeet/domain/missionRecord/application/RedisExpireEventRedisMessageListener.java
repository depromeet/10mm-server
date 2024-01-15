package com.depromeet.domain.missionRecord.application;

import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.global.common.constants.RedisExpireEventConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisExpireEventRedisMessageListener implements MessageListener {
    private final MissionRecordRepository missionRecordRepository;

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
        String event = message.toString().split(":")[1];

        if (!event.startsWith(
                RedisExpireEventConstants.EXPIRE_EVENT_IMAGE_UPLOAD_TIME_END.getValue())) {
            return;
        }

        Long missionRecordId = Long.valueOf(event.split("_")[6]);
        missionRecordRepository.deleteById(missionRecordId);
    }
}
