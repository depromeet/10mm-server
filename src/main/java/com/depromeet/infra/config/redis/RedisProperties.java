package com.depromeet.infra.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
	private String host;
	private int port;
	private String password;
}
