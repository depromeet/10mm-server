package com.depromeet.infra.config.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.depromeet.infra.config.redis.RedisProperties;
import com.depromeet.infra.config.storage.StorageProperties;

@EnableConfigurationProperties({StorageProperties.class, RedisProperties.class})
@Configuration
public class PropertiesConfig {}
