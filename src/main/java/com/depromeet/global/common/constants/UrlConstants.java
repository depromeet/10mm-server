package com.depromeet.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
    PROD_SERVER_URL("https://api.10mm.today"),
    DEV_SERVER_URL("https://dev-api.10mm.today"),
    LOCAL_SERVER_URL("http://localhost:8080"),

    PROD_DOMAIN_URL("https://www.10mm.today"),
    DEV_DOMAIN_URL("https://www.dev.10mm.today"),
    LOCAL_DOMAIN_URL("http://localhost:3000"),
    LOCAL_SECURE_DOMAIN_URL("https://localhost:3000"),

    NGROK_DOMAIN_URL("https://*.ngrok-free.app"),

    IMAGE_DOMAIN_URL("https://image.10mm.today"),
    ;

    private String value;
}
