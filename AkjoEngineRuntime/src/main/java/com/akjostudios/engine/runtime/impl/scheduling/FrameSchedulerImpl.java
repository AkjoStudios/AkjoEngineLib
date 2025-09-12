package com.akjostudios.engine.runtime.impl.scheduling;

import com.akjostudios.engine.api.common.mailbox.Mailbox;
import com.akjostudios.engine.api.common.cancel.Cancellable;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.MAIN_THREAD_NAME;

@RequiredArgsConstructor
public final class FrameSchedulerImpl implements FrameScheduler {
    private static final class Task implements Cancellable {
        private final Runnable runnable;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private long remainingFrames;
        private final boolean recurring;

        Task(@NotNull Runnable runnable, long frames, boolean recurring) {
            this.runnable = runnable;
            this.remainingFrames = frames;
            this.recurring = recurring;
        }

        @Override
        public boolean cancel() { return cancelled.compareAndSet(false, true); }

        @Override
        public boolean isCancelled() { return cancelled.get(); }
    }

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final AtomicLong frame = new AtomicLong(0);

    private final Mailbox mailbox;

    private Runnable postFrameTask;

    @Override
    public @NotNull Cancellable everyFrame(@NotNull Runnable task) {
        Task t = new Task(task, 1, true);
        tasks.add(t);
        return t;
    }

    @Override
    public @NotNull Cancellable afterFrames(int frames, @NotNull Runnable task) {
        Task t = new Task(task, Math.max(0, frames), false);
        tasks.add(t);
        return t;
    }

    @Override
    public long currentFrame() { return frame.get(); }

    public void onFrame() {
        frame.incrementAndGet();

        for (Task task : tasks) {
            if (task.isCancelled()) { continue; }
            if (task.remainingFrames > 0) { task.remainingFrames--; }
            if (task.remainingFrames == 0) {
                mailbox.postOrThrow(() -> {
                    if (!task.isCancelled()) { task.runnable.run(); }
                    if (task.recurring) { task.remainingFrames = 1; }
                    else { task.cancel(); }
                });
            }
        }

        if (postFrameTask != null) { mailbox.postOrThrow(postFrameTask); }
        tasks.removeIf(Task::isCancelled);
    }

    /**
     * Sets the post-frame task for this frame scheduler.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setPostFrameTask(
            @NotNull Object token,
            @NotNull Runnable task
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Post frame task is not being initialized on main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        this.postFrameTask = task;
    }
}