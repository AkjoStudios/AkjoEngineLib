package com.akjostudios.engine.runtime.impl.scheduling;

import com.akjostudios.engine.api.common.Mailbox;
import com.akjostudios.engine.api.common.cancel.Cancellable;
import com.akjostudios.engine.api.scheduling.TickScheduler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public final class TickSchedulerImpl implements TickScheduler {
    private static final class Task implements Cancellable {
        private final Runnable runnable;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private long remainingTicks;
        private final boolean recurring;

        Task(@NotNull Runnable runnable, long ticks, boolean recurring) {
            this.runnable = runnable;
            this.remainingTicks = ticks;
            this.recurring = recurring;
        }

        @Override
        public boolean cancel() { return cancelled.compareAndSet(false, true); }

        @Override
        public boolean isCancelled() { return cancelled.get(); }
    }

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final AtomicLong tick = new AtomicLong(0);

    private final Mailbox logicMailbox;

    @Override
    public @NotNull Cancellable everyTick(@NotNull Runnable task) {
        Task t = new Task(task, 1, true);
        tasks.add(t);
        return t;
    }

    @Override
    public @NotNull Cancellable afterTicks(int ticks, @NotNull Runnable task) {
        Task t = new Task(task, Math.max(0, ticks), false);
        tasks.add(t);
        return t;
    }

    @Override
    public long currentTick() { return tick.get(); }

    public void onTick() {
        tick.incrementAndGet();
        for (Task task : tasks) {
            if (task.isCancelled()) { continue; }
            if (task.remainingTicks > 0) { task.remainingTicks--; }
            if (task.remainingTicks == 0) {
                logicMailbox.postOrThrow(() -> {
                    if (!task.isCancelled()) { task.runnable.run(); }
                    if (task.recurring) { task.remainingTicks = 1; }
                    else { task.cancel(); }
                });
            }
        }
        tasks.removeIf(Task::isCancelled);
    }
}