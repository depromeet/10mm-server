package com.depromeet.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SwaggerUrlConstants {
    SWAGGER_RESOURCES_URL("/swagger-resources/**"),
    SWAGGER_UI_URL("/swagger-ui/**"),
    SWAGGER_API_DOCS_URL("/v3/api-docs/**"),
    ;

    private String value;
}
