package com.depromeet;

import com.depromeet.infra.config.redis.RedisConfig;
import com.depromeet.infra.config.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({RedisProperties.class})
@Import({RedisConfig.class})
public class TestRedisConfig {}
