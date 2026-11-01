package com.depromeet.infra.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.redis")
public record CacheRedisProperties(String host, int port, String password, boolean enabled) {}
