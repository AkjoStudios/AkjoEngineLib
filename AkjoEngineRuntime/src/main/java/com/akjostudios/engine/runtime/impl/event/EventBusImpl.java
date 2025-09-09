package com.akjostudios.engine.runtime.impl.event;

import com.akjostudios.engine.api.event.*;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.runtime.util.ImmutableArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class EventBusImpl implements EventBus {
    private final Threading threading;
    private final Scheduler scheduler;
    private final Logger log;

    private final Map<Class<? extends Event>, ListenerList<? extends Event>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<? extends Event>, EventLane> defaultLanes = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> EventSubscription subscribe(
            @NotNull Class<T> type,
            @NotNull EventListener<T> listener
    ) {
        ListenerList<T> list = getOrCreate(type);
        Subscription<T> subscription = new Subscription<>(type, listener, list);
        list.subscriptions.add(subscription);
        return subscription;
    }

    @Override
    public void publish(@NotNull Event event) {
        EventLane lane = defaultLanes.getOrDefault(event.getClass(), EventLane.LOGIC);
        publish(event, lane);
    }

    @Override
    public void publish(@NotNull Event event, @NotNull EventLane lane) {
        Runnable dispatch = () -> dispatch(event);
        switch (lane) {
            case RENDER -> scheduler.render().afterFrames(1, dispatch);
            case LOGIC -> scheduler.logic().afterTicks(1, dispatch);
            case AUDIO -> scheduler.audio().afterFrames(1, dispatch);
            case WORKER -> threading.runOnWorker(dispatch);
        }
    }

    @Override
    public <T extends Event> void publishImmediate(@NotNull T event) { dispatch(event); }

    /**
     * Sets the given lane as the default lane for the given event type.
     */
    public void setDefaultLane(@NotNull Class<? extends Event> type, @NotNull EventLane lane) {
        defaultLanes.put(type, lane);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> ListenerList<T> getOrCreate(@NotNull Class<T> type) {
        return (ListenerList<T>) listeners.computeIfAbsent(type, k -> new ListenerList<T>());
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void dispatch(@NotNull T event) {
        ListenerList<T> list = (ListenerList<T>) listeners.get(event.getClass());
        if (list == null) { return; }

        list.subscriptions.forEach(subscription -> {
            if (!subscription.isActive()) { return; }
            try {
                subscription.listener.onEvent(event);
            } catch (Throwable t) {
                try {
                    log.error(t, "Event listener threw an exception for {}!", event.getClass().getSimpleName());
                } catch (Throwable ignored) {}
            }
        });
    }

    private static final class ListenerList<T extends Event> {
        public final ImmutableArrayList<Subscription<T>> subscriptions = new ImmutableArrayList<>();
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    private static final class Subscription<T extends Event> implements EventSubscription {
        private final Class<T> type;
        private final EventListener<T> listener;
        private final ListenerList<T> owner;
        private final AtomicBoolean active = new AtomicBoolean(true);

        @Override
        public void close() {
            if (active.compareAndSet(true, false)) {
                owner.subscriptions.remove(this);
            }
        }

        @Override
        public boolean isActive() { return active.get(); }
    }
}