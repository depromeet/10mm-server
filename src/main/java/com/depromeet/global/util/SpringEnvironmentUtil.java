package com.depromeet.global.util;

import com.depromeet.global.common.constants.EnvironmentConstants;
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

    private final List<String> PROD_AND_DEV =
            List.of(EnvironmentConstants.PROD.getValue(), EnvironmentConstants.DEV.getValue());

    public String getCurrentProfile() {
        return getActiveProfiles()
                .filter(
                        profile ->
                                profile.equals(EnvironmentConstants.PROD.getValue())
                                        || profile.equals(EnvironmentConstants.DEV.getValue()))
                .findFirst()
                .orElse(EnvironmentConstants.LOCAL.getValue());
    }

    public Boolean isProdProfile() {
        return getActiveProfiles().anyMatch(EnvironmentConstants.PROD.getValue()::equals);
    }

    public Boolean isDevProfile() {
        return getActiveProfiles().anyMatch(EnvironmentConstants.DEV.getValue()::equals);
    }

    public Boolean isProdAndDevProfile() {
        return getActiveProfiles().anyMatch(PROD_AND_DEV::contains);
    }

    private Stream<String> getActiveProfiles() {
        return Arrays.stream(environment.getActiveProfiles());
    }
}
