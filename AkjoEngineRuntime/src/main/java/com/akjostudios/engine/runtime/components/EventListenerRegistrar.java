package com.akjostudios.engine.runtime.components;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.event.EventHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j(topic = "engine / EventListenerRegistrar")
public class EventListenerRegistrar implements DestructionAwareBeanPostProcessor, PriorityOrdered {
    @Getter
    private final List<Registration> registrations = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        for (Method method : targetClass.getMethods()) {
            EventHandler annotation = AnnotationUtils.findAnnotation(method, EventHandler.class);
            if (annotation == null) { continue; }

            if (method.getParameterCount() != 1) { continue; }
            if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) { continue; }

            if (!method.canAccess(bean)) { method.setAccessible(true); }

            registrations.add(new Registration((Class<? extends Event>) method.getParameterTypes()[0], bean, method));
        }

        return bean;
    }

    @Override
    public int getOrder() { return PriorityOrdered.LOWEST_PRECEDENCE; }

    @Override
    public void postProcessBeforeDestruction(@NotNull Object bean, @NotNull String beanName) throws BeansException {}

    public record Registration(
            @NotNull Class<? extends Event> eventType,
            @NotNull Object bean,
            @NotNull Method method
    ) {}
}