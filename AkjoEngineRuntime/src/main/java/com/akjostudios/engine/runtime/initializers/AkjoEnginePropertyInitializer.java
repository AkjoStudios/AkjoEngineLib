package com.akjostudios.engine.runtime.initializers;

import com.akjostudios.engine.runtime.AkjoEngineAppProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

@Slf4j
public class AkjoEnginePropertyInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String PROPERTY_SOURCE_FILTER = "optional:classpath:/";

    private static final Set<String> ALLOWED_PREFIXES = Set.of(
            "engine."
    );

    @Override
    public void initialize(@NotNull GenericApplicationContext context) {
        log.info("ℹ️ Initializing and validating application properties...");

        ConfigurableEnvironment env = context.getEnvironment();

        for (PropertySource<?> ps : env.getPropertySources()) {
            if (ps.getName().contains(PROPERTY_SOURCE_FILTER) && ps instanceof EnumerablePropertySource<?> eps) {
                for (String key : eps.getPropertyNames()) {
                    if (ALLOWED_PREFIXES.stream().noneMatch(key::startsWith)) {
                        throw new IllegalStateException(
                                "❗ Property '" + key + "' is not allowed in application.yml. " +
                                        "Only properties with the following prefixes are allowed: " + ALLOWED_PREFIXES +
                                        ". Please remove this property."
                        );
                    }
                }
            }
        }

        URL resource = Thread.currentThread().getContextClassLoader().getResource("app.yml");
        if (resource == null) {
            throw new IllegalStateException("❗ app.yml not found in resources. Please ensure it is present.");
        }

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
                .registerModule(new Jdk8Module());

        AkjoEngineAppProperties appProperties;
        try (InputStream is = resource.openStream()) {
            if (is.available() == 0) {
                appProperties = new AkjoEngineAppProperties();
            } else {
                appProperties = yamlMapper.readValue(is, AkjoEngineAppProperties.class);
            }
            context.registerBean(AkjoEngineAppProperties.class, () -> appProperties);
        } catch (Exception e) {
            throw new IllegalStateException("❗ Failed to load or parse app.yml. Please ensure it is valid YAML.", e);
        }

        log.info("✅ Application properties initialized and validated successfully.");
    }
}