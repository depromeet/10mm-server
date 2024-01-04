package com.depromeet.infra.config.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
}
