package com.depromeet.global.common.constants;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvironmentConstants {

    public static final String PROD = "prod";
    public static final String DEV = "dev";
    public static final String LOCAL = "local";
    public static final List<String> PROD_AND_DEV = List.of(PROD, DEV);
}
