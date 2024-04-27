package com.depromeet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
public class NoTransactionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        var applicationContext = SpringExtension.getApplicationContext(extensionContext);
        cleanDatabase(applicationContext);
    }

    private static void cleanDatabase(ApplicationContext applicationContext) {
        try {
            DatabaseCleaner.clear(applicationContext);
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("Database Cleaning not supported.");
        }
    }
}
