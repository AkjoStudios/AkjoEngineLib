package com.akjostudios.engine.runtime.initializers;

import com.akjostudios.engine.api.IAkjoApplication;
import com.akjostudios.engine.runtime.AkjoEngineAppProperties;
import com.akjostudios.engine.runtime.AkjoEngineRuntime;
import com.akjostudios.engine.runtime.impl.AkjoApplicationContext;
import com.akjostudios.engine.runtime.impl.DefaultAkjoApplication;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "engine / AkjoEngineRuntimeInitializer")
public class AkjoEngineRuntimeInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String BASE_PACKAGE_PROPERTY = "engine.base-package";

    @Override
    public void initialize(@NotNull GenericApplicationContext context) {
        log.info("🔧 Initializing AkjoEngine Runtime...");

        context.registerBean(AkjoApplicationContext.class);

        context.registerBean(AkjoEngineRuntime.class, () -> {
            IAkjoApplication application = resolveApplication(
                    context.getEnvironment().getProperty(BASE_PACKAGE_PROPERTY)
            );
            context.registerBean(IAkjoApplication.class, () -> application);
            AkjoApplicationContext appContext = context.getBean(AkjoApplicationContext.class);
            AkjoEngineAppProperties appProperties = context.getBean(AkjoEngineAppProperties.class);
            return new AkjoEngineRuntime(
                    application, appContext, appProperties,
                    context.getEnvironment().getSystemProperties()
            );
        });

        log.info("🔥 AkjoEngine Runtime initialized. Starting application...");
    }

    private IAkjoApplication resolveApplication(String basePackage) {
        Map<String, IAkjoApplication> applications = new HashMap<>();

        Reflections reflections = (basePackage != null && !basePackage.isBlank())
                ? new Reflections(basePackage)
                : new Reflections("");
        reflections.getSubTypesOf(IAkjoApplication.class).forEach(application -> {
            try {
                if (!application.isInterface() && !Modifier.isAbstract(application.getModifiers())) {
                    IAkjoApplication instance = application.getDeclaredConstructor().newInstance();
                    applications.put(application.getSimpleName(), instance);
                }
            } catch (Exception e) {
                log.error("Failed to instantiate application class '{}'!", application.getName(), e);
            }
        });

        if (applications.isEmpty()) {
            log.warn("❓ No Application found - please define one by extending the AkjoApplication class.");
            return new DefaultAkjoApplication();
        }
        if (applications.size() > 1) {
            String names = String.join(", ", applications.keySet());
            throw new IllegalStateException("❗ Multiple Applications found (" + names + ") - only one is allowed.");
        }

        Map.Entry<String, IAkjoApplication> entry = applications.entrySet().iterator().next();
        log.debug("Using AkjoApplication bean provided by consumer: {}", entry.getKey());
        return entry.getValue();
    }
}