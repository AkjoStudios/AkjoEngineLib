package com.akjostudios.engine.runtime.initializers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
public class AkjoEnginePackageInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String BASE_PACKAGE_PROPERTY = "engine.base-package";

    @Override
    public void initialize(@NotNull GenericApplicationContext context) {
        Environment env = context.getEnvironment();
        String basePackage = env.getProperty(BASE_PACKAGE_PROPERTY);

        if (basePackage == null || basePackage.isBlank()) {
            throw new IllegalStateException("⚠️ No 'engine.base-package' property found! Please set it in your application properties.");
        }

        log.info("🔍 Scanning consumer package '{}' for components...", basePackage);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
        scanner.scan(basePackage);
        log.info("✅ Finished scanning consumer package '{}' for components.", basePackage);
    }
}