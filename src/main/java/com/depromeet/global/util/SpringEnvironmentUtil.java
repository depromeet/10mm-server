package com.depromeet.global.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEnvironmentUtil {
    private final Environment environment;

    public static final String PROD = "prod";
    public static String DEV = "dev";
    public static String LOCAL = "local";

    private final List<String> PROD_AND_DEV = List.of(PROD, DEV);

    public String getCurrentProfile() {
        return getActiveProfiles()
                .filter(profile -> profile.equals(PROD) || profile.equals(DEV))
                .findFirst()
                .orElse(LOCAL);
    }

    public Boolean isProdProfile() {
        return getActiveProfiles().anyMatch(PROD::equals);
    }

    public Boolean isDevProfile() {
        return getActiveProfiles().anyMatch(DEV::equals);
    }

    public Boolean isProdAndDevProfile() {
        return getActiveProfiles().anyMatch(PROD_AND_DEV::contains);
    }

    private Stream<String> getActiveProfiles() {
        return Arrays.stream(environment.getActiveProfiles());
    }
}
