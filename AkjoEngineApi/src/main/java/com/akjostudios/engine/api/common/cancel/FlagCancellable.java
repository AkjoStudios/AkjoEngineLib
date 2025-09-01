package com.akjostudios.engine.api.common.cancel;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public final class FlagCancellable implements Cancellable {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public boolean cancel() { return cancelled.compareAndSet(false, true); }

    @Override
    public boolean isCancelled() { return cancelled.get(); }

    public boolean tryRun(@NotNull Runnable task) {
        if (isCancelled()) { return false; }
        task.run();
        return true;
    }
}