package com.depromeet.global.annotation;

import static com.depromeet.global.common.constants.EnvironmentConstants.LOCAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional({OnProfileCondition.class})
public @interface ConditionalOnProfile {
    String[] value() default {LOCAL};
}
