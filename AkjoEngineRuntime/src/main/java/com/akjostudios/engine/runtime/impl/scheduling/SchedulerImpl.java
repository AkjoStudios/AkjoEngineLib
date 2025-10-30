package com.akjostudios.engine.runtime.impl.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import com.akjostudios.engine.api.common.cancel.FlagCancellable;
import com.akjostudios.engine.api.common.mailbox.Mailbox;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.scheduling.SchedulerLane;
import com.akjostudios.engine.api.scheduling.TickScheduler;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Accessors(fluent = true)
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class SchedulerImpl implements Scheduler {
    private final ScheduledExecutorService timer;
    private final Mailbox logicMailbox;

    private final FrameScheduler renderScheduler;
    private final TickScheduler logicScheduler;
    private final FrameScheduler audioScheduler;

    @Override
    public @NotNull Cancellable runDelayed(@NotNull Runnable task, long delayMillis) {
        FlagCancellable cancellable = new FlagCancellable();
        ScheduledFuture<?> future = timer.schedule(() -> {
            if (!cancellable.isCancelled()) {
                logicMailbox.postOrThrow(task);
            }
        }, Math.max(0, delayMillis), TimeUnit.MILLISECONDS);
        return wrapFuture(cancellable, future);
    }

    @Override
    public @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, double hertz) {
        long periodMs = (long) Math.max(1.0, 1000.0 / Math.max(1.0, hertz));
        return runAtFixedRate(task, periodMs, periodMs);
    }

    @Override
    public @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, long initialDelayMillis, long periodMillis) {
        FlagCancellable cancellable = new FlagCancellable();
        ScheduledFuture<?> future = timer.scheduleAtFixedRate(() -> {
            if (!cancellable.isCancelled()) {
                logicMailbox.postOrThrow(task);
            }
        }, Math.max(0, initialDelayMillis), Math.max(1, periodMillis), TimeUnit.MILLISECONDS);
        return wrapFuture(cancellable, future);
    }

    @Override
    public @NotNull Cancellable runOnceNextTick(@NotNull Runnable task) {
        return logicScheduler.afterTicks(1, task);
    }

    @Override
    public @NotNull Cancellable runOnceNextFrame(@NotNull Runnable task, @NotNull SchedulerLane lane) {
        return switch (lane) {
            case RENDER -> renderScheduler.afterFrames(1, task);
            case AUDIO -> audioScheduler.afterFrames(1, task);
        };
    }

    @Override
    public boolean isScheduled(@NotNull Cancellable task) {
        return (task instanceof FlagCancellable cancellable) && !cancellable.isCancelled();
    }

    @Override
    public @NotNull FrameScheduler render() { return renderScheduler; }

    @Override
    public @NotNull TickScheduler logic() { return logicScheduler; }

    @Override
    public @NotNull FrameScheduler audio() { return audioScheduler; }

    private static @NotNull Cancellable wrapFuture(@NotNull FlagCancellable cancellable, @NotNull ScheduledFuture<?> future) {
        return new Cancellable() {
            @Override
            public boolean cancel() {
                boolean isCancelled = cancellable.cancel();
                future.cancel(false);
                return isCancelled;
            }

            @Override
            public boolean isCancelled() { return cancellable.isCancelled(); }
        };
    }
}