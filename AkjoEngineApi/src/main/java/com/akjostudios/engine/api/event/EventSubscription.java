package com.akjostudios.engine.api.event;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface EventSubscription extends AutoCloseable {
    /**
     * Cancels the subscription and unregisters the event listener.
     */
    @Override void close();

    /**
     * @return If the subscription is still active.
     */
    boolean isActive();
}