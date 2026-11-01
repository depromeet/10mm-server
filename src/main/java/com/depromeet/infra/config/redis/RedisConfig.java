package com.depromeet.infra.config.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisProperties redisProperties;
    private final CacheRedisProperties cacheRedisProperties;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return createConnectionFactory(
                redisProperties.host(), redisProperties.port(), redisProperties.password());
    }

    @Bean("cacheRedisConnectionFactory")
    public RedisConnectionFactory cacheRedisConnectionFactory() {
        return createConnectionFactory(
                cacheRedisProperties.host(),
                cacheRedisProperties.port(),
                cacheRedisProperties.password());
    }

    @Bean("cacheStringRedisTemplate")
    public StringRedisTemplate cacheStringRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory")
                    RedisConnectionFactory cacheRedisConnectionFactory) {
        return new StringRedisTemplate(cacheRedisConnectionFactory);
    }

    private RedisConnectionFactory createConnectionFactory(String host, int port, String password) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            redisConfig.setPassword(password);
        }
        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(1))
                        .shutdownTimeout(Duration.ZERO)
                        .build();
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
}
