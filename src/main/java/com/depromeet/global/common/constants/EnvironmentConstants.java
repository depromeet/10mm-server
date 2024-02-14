package com.depromeet.global.common.constants;

import static com.depromeet.global.common.constants.EnvironmentConstants.Constants.*;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum EnvironmentConstants {
    PROD(PROD_ENV),
    DEV(DEV_ENV),
    LOCAL(LOCAL_ENV);

    private final String value;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Constants {
        public static final String PROD_ENV = "prod";
        public static final String DEV_ENV = "dev";
        public static final String LOCAL_ENV = "local";
        public static final List<String> PROD_AND_DEV_ENV = List.of(PROD_ENV, DEV_ENV);
    }
}
