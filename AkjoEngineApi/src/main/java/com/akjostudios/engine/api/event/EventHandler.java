package com.akjostudios.engine.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface EventHandler {
    Class<? extends Event> value() default Event.class;
    EventLane lane() default EventLane.LOGIC;
}