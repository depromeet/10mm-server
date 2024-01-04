package com.depromeet.infra.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
	private String accessKey;
	private String secretKey;
	private String region;
	private String bucket;
}
