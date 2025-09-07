package com.akjostudios.engine.api.event;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface EventSubscription extends AutoCloseable {
    @Override void close();
    boolean isActive();
}