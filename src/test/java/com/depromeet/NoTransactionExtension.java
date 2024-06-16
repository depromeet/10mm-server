package com.depromeet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class NoTransactionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        var applicationContext = SpringExtension.getApplicationContext(extensionContext);

        if (!validateDataJpaTestAnnotationExists(extensionContext)) {
            validateTransactionalAnnotationExists(extensionContext);
        }
        cleanDatabase(applicationContext);
    }

    private static boolean validateDataJpaTestAnnotationExists(ExtensionContext extensionContext) {
        return TestContextAnnotationUtils.hasAnnotation(
                extensionContext.getRequiredTestClass(), DataJpaTest.class);
    }

    // Transactional 어노테이션이 존재하는지 검증하는 메서드
    private static void validateTransactionalAnnotationExists(ExtensionContext extensionContext) {
        if (TestContextAnnotationUtils.hasAnnotation(
                        extensionContext.getRequiredTestClass(), Transactional.class)
                || TestContextAnnotationUtils.hasAnnotation(
                        extensionContext.getRequiredTestClass(),
                        jakarta.transaction.Transactional.class)) {
            Assertions.fail(
                    "테스트 클래스에 @Transactional 또는 @jakarta.transaction.Transactional 어노테이션이 존재합니다.");
        }

        if (AnnotatedElementUtils.hasAnnotation(
                        extensionContext.getRequiredTestMethod(), Transactional.class)
                || AnnotatedElementUtils.hasAnnotation(
                        extensionContext.getRequiredTestMethod(),
                        jakarta.transaction.Transactional.class)) {
            Assertions.fail(
                    "테스트 메서드에 @Transactional 또는 @jakarta.transaction.Transactional 어노테이션이 존재합니다.");
        }
    }

    private static void cleanDatabase(ApplicationContext applicationContext) {
        try {
            DatabaseCleaner.clear(applicationContext);
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("Database Cleaning not supported.");
        }
    }
}
