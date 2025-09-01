package com.akjostudios.engine.runtime.impl.lifecycle;

import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class LifecycleImpl implements Lifecycle {
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    private final Logger log;

    private final Supplier<Boolean> runningSupplier;
    private final BiConsumer<String, Throwable> stopHandler;

    /**
     * @apiNote This method does not work in the initialization phase.
     */
    @Override
    public void stopApplication(@Nullable Throwable throwable, @Nullable String reason) {
        if (!stopping.compareAndSet(false, true)) { return; }
        try {
            stopHandler.accept(reason, throwable);
        } catch (Throwable t) {
            log.error("❗ Failed to stop application!");
        }
    }

    @Override
    public boolean isRunning() { return runningSupplier.get(); }

    @Override
    public boolean isStopping() { return stopping.get(); }
}