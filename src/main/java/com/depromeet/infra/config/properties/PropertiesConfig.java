package com.depromeet.infra.config.properties;

import com.depromeet.infra.config.jwt.JwtProperties;
import com.depromeet.infra.config.redis.RedisProperties;
import com.depromeet.infra.config.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
    StorageProperties.class,
    RedisProperties.class,
    JwtProperties.class,
    //    AppleProperties.class
})
@Configuration
public class PropertiesConfig {}
