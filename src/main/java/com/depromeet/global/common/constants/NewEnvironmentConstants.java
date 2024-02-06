package com.depromeet.global.common.constants;

import java.util.List;

public class NewEnvironmentConstants {

    private NewEnvironmentConstants() {}

    public static final String PROD = "prod";
    public static final String DEV = "dev";
    public static final String LOCAL = "local";
    public static final List<String> PROD_AND_DEV = List.of(PROD, DEV);
}
