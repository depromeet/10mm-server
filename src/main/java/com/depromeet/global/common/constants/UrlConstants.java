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
    ;

    private String value;
}
