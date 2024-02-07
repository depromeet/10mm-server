package com.depromeet.global.util;

import static com.depromeet.global.common.constants.EnvironmentConstants.*;

import java.util.Arrays;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEnvironmentUtil {

    private final Environment environment;

    public String getCurrentProfile() {
        return getActiveProfiles()
                .filter(profile -> profile.equals(PROD) || profile.equals(DEV))
                .findFirst()
                .orElse(LOCAL);
    }

    public boolean isProdProfile() {
        return getActiveProfiles().anyMatch(PROD::equals);
    }

    public boolean isDevProfile() {
        return getActiveProfiles().anyMatch(DEV::equals);
    }

    public boolean isProdAndDevProfile() {
        return getActiveProfiles().anyMatch(PROD_AND_DEV::contains);
    }

    private Stream<String> getActiveProfiles() {
        return Arrays.stream(environment.getActiveProfiles());
    }
}
