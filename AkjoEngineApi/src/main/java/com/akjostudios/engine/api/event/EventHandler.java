package com.akjostudios.engine.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler that runs on the given {@link EventLane event lane} (default: LOGIC).
 * @apiNote Method needs exactly one parameter, the event object of the given type.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface EventHandler {
    Class<? extends Event> value() default Event.class;
    EventLane lane() default EventLane.LOGIC;
}