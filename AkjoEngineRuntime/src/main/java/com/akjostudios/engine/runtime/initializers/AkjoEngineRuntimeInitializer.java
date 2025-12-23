package com.akjostudios.engine.runtime.initializers;

import com.akjostudios.engine.api.IAkjoApplication;
import com.akjostudios.engine.runtime.AkjoEngineProjectProperties;
import com.akjostudios.engine.runtime.AkjoEngineRuntime;
import com.akjostudios.engine.runtime.components.EventListenerRegistrar;
import com.akjostudios.engine.runtime.impl.AkjoApplicationContext;
import com.akjostudios.engine.runtime.impl.DefaultAkjoApplication;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import java.beans.Introspector;
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
            IAkjoApplication application = resolveApplication(context);
            AkjoApplicationContext appContext = context.getBean(AkjoApplicationContext.class);
            AkjoEngineProjectProperties appProperties = context.getBean(AkjoEngineProjectProperties.class);
            EventListenerRegistrar eventListenerRegistrar = context.getBean(EventListenerRegistrar.class);

            return new AkjoEngineRuntime(
                    application, appContext, appProperties,
                    context.getEnvironment().getSystemProperties(),
                    eventListenerRegistrar.getRegistrations()
            );
        });

        log.info("🔥 AkjoEngine Runtime initialized. Starting application...");
    }

    private IAkjoApplication resolveApplication(@NotNull GenericApplicationContext context) {
        String basePackage = context.getEnvironment().getProperty(BASE_PACKAGE_PROPERTY);
        Map<String, IAkjoApplication> applications = new HashMap<>();

        Reflections reflections = (basePackage != null && !basePackage.isBlank())
                ? new Reflections(basePackage)
                : new Reflections("");
        reflections.getSubTypesOf(IAkjoApplication.class).forEach(application -> {
            try {
                if (!application.isInterface() && !Modifier.isAbstract(application.getModifiers())) {
                    String applicationName = Introspector.decapitalize(application.getSimpleName());
                    if (context.containsBeanDefinition(applicationName)) {
                        applications.put(applicationName, context.getBean(application));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to instantiate application class '{}'!", application.getName(), e);
            }
        });

        if (applications.isEmpty()) {
            log.warn("❓ No Application component found - please define one by extending the AkjoApplication class and annotating it with @Component.");
            return new DefaultAkjoApplication();
        }
        if (applications.size() > 1) {
            String names = String.join(", ", applications.keySet());
            throw new IllegalStateException("❗ Multiple Application components found (" + names + ") - only one is allowed.");
        }

        Map.Entry<String, IAkjoApplication> entry = applications.entrySet().iterator().next();
        log.debug("Using AkjoApplication bean provided by consumer: {}", entry.getKey());
        return entry.getValue();
    }
}