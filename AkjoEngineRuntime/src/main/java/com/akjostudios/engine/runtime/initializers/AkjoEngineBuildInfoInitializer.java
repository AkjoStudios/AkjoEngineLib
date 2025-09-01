package com.akjostudios.engine.runtime.initializers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Slf4j(topic = "engine / AkjoEngineBuildInfoInitializer")
public class AkjoEngineBuildInfoInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String BUILD_INFO_RESOURCE = "META-INF/build-info.properties";

    @Override
    public void initialize(@NotNull GenericApplicationContext context) {
        try {
            log.info("📦 Loading artifact from build info...");
            ClassPathResource resource = new ClassPathResource(BUILD_INFO_RESOURCE);

            if (!resource.exists()) {
                throw new IllegalStateException("❗ build-info.properties not found in classpath! This is likely a bug in the engine - please report it using the issue tracker.");
            }

            Properties properties = PropertiesLoaderUtils.loadProperties(resource);

            String group = properties.getProperty("build.group");
            String artifact = properties.getProperty("build.artifact");
            String version = properties.getProperty("build.version");
            String engineVersion = properties.getProperty("build.engine.version");

            context.getEnvironment().getSystemProperties().put("spring.application.group", group);
            context.getEnvironment().getSystemProperties().put("spring.application.name", artifact);
            context.getEnvironment().getSystemProperties().put("spring.application.version", version);
            context.getEnvironment().getSystemProperties().put("engine.version", engineVersion);
            log.info("✅ Loaded artifact '{}' with version '{}' using engine version '{}'.", artifact, version, engineVersion);
        } catch (IOException e) {
            throw new IllegalStateException("❗ Failed to load build-info.properties! This is likely a bug in the engine - please report it using the issue tracker.", e);
        }
    }
}