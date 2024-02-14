package com.depromeet.global.annotation;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnProfileCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        String[] targetProfiles = getTargetProfiles(metadata);

        return Arrays.stream(targetProfiles)
                .anyMatch(targetProfile -> Arrays.asList(activeProfiles).contains(targetProfile));
    }

    private String[] getTargetProfiles(AnnotatedTypeMetadata metadata) {
        return (String[])
                requireNonNull(
                                metadata.getAnnotationAttributes(
                                        ConditionalOnProfile.class.getName()))
                        .get("value");
    }
}
