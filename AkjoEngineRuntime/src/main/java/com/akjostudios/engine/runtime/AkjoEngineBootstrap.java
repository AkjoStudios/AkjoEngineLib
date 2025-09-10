package com.akjostudios.engine.runtime;

import com.akjostudios.engine.runtime.initializers.AkjoEngineBuildInfoInitializer;
import com.akjostudios.engine.runtime.initializers.AkjoEngineComponentInitializer;
import com.akjostudios.engine.runtime.initializers.AkjoEnginePropertyInitializer;
import com.akjostudios.engine.runtime.initializers.AkjoEngineRuntimeInitializer;
import com.akjostudios.engine.runtime.util.DateTimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Locale;
import java.util.Map;

public class AkjoEngineBootstrap {
    private static final String DATE_TIME_PATTERN = DateTimeFormatUtil.safeLocalizedPattern(Locale.getDefault(Locale.Category.FORMAT));

    private static final String BASE_PACKAGE = "com.akjostudios.engine.runtime";

    private static final Map<String, Object> DEFAULT_PROPS = Map.ofEntries(
            Map.entry("spring.main.allow-bean-definition-overriding", "true"),
            Map.entry("spring.main.lazy-initialization", "true"),
            Map.entry("spring.main.banner-mode", "console"),
            Map.entry("spring.output.ansi.enabled", "always"),
            Map.entry("logging.pattern.console", "%clr(%d{" + DATE_TIME_PATTERN + "}){faint} %clr(%5p) %clr([%10.10t]){faint} %clr(%-50.50logger{49}){cyan} %clr(:){faint} %m%n%wEx"),
            Map.entry("logging.level.com.akjostudios.engine.runtime.AkjoEngineBootstrap", "WARN"),
            Map.entry("logging.level.org.reflections.Reflections", "WARN"),
            Map.entry("logging.level.org.springframework.boot", "OFF")
    );

    public static void main(String[] args) {
        new SpringApplicationBuilder(AkjoEngineConfig.class).initializers(
                new AkjoEnginePropertyInitializer(),
                new AkjoEngineBuildInfoInitializer(),
                new AkjoEngineComponentInitializer(),
                new AkjoEngineRuntimeInitializer()
        ).properties(DEFAULT_PROPS).run(args);
    }

    @Configuration
    @RequiredArgsConstructor
    @ComponentScan(basePackages = BASE_PACKAGE)
    @EnableConfigurationProperties(AkjoEngineProperties.class)
    static class AkjoEngineConfig {
        @EventListener
        public void onApplicationStarted(@NotNull ApplicationStartedEvent event) {
            event.getApplicationContext().getBean(AkjoEngineRuntime.class).start();
        }
    }
}