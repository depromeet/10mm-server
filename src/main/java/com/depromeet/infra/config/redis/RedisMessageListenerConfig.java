package com.depromeet.infra.config.redis;

import com.depromeet.domain.missionRecord.application.RedisExpireEventRedisMessageListener;
import com.depromeet.global.common.constants.RedisExpireEventConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisMessageListenerConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            RedisExpireEventRedisMessageListener redisExpireEventRedisMessageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(
                redisExpireEventRedisMessageListener,
                new PatternTopic(RedisExpireEventConstants.REDIS_EXPIRE_EVENT_PATTERN.getValue()));
        return container;
    }
}
