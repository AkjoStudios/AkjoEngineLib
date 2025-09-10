package com.akjostudios.engine.runtime.initializers;

import com.akjostudios.engine.api.context.Component;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

import java.beans.Introspector;
import java.lang.reflect.Modifier;

@Slf4j(topic = "engine / AkjoEngineComponentInitializer")
public class AkjoEngineComponentInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String BASE_PACKAGE_PROPERTY = "engine.base-package";

    @Override
    public void initialize(@NotNull GenericApplicationContext context) {
        Environment environment = context.getEnvironment();
        String basePackage = environment.getProperty(BASE_PACKAGE_PROPERTY);

        log.info("🔍 Scanning consumer package '{}' for components...", basePackage);

        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        int count = 0;
        for (Class<?> component : reflections.getTypesAnnotatedWith(Component.class)) {
            if (component.isInterface() || Modifier.isAbstract(component.getModifiers())) { continue; }

            String beanName = Introspector.decapitalize(component.getSimpleName());
            if (context.containsBeanDefinition(beanName)) { continue; }

            context.registerBean(beanName, component);
            count++;
        }

        log.info("✅ Found and successfully registered {} component(s).", count);
    }
}