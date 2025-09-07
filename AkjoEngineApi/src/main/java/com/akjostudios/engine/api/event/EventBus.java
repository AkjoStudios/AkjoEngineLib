package com.akjostudios.engine.api.event;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface EventBus {
    <T extends Event> EventSubscription subscribe(
            @NotNull Class<T> type,
            @NotNull EventLane lane,
            @NotNull EventListener<T> listener
    );

    void publish(@NotNull Event event);

    void publish(@NotNull Event event, @NotNull EventLane lane);

    <T extends Event> void publishImmediate(@NotNull T event);
}