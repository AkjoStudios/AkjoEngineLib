package com.akjostudios.engine.api.event;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface EventBus {
    /**
     * Subscribes an event listener to the given event type.
     * @return The subscription to close if needed.
     */
    <T extends Event> EventSubscription subscribe(
            @NotNull Class<T> type,
            @NotNull EventListener<T> listener
    );

    /**
     * Publishes an event on the event bus to the default event lane.
     */
    void publish(@NotNull Event event);

    /**
     * Publishes an event on the event bus to the given event lane.
     */
    void publish(@NotNull Event event, @NotNull EventLane lane);

    /**
     * Publishes the given event immediately on the current thread.
     */
    <T extends Event> void publishImmediate(@NotNull T event);
}