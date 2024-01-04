package com.depromeet.infra.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(String accessKey, String secretKey, String region, String bucket) {}
