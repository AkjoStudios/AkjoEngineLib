package com.akjostudios.engine.api.common.mailbox;

import com.akjostudios.engine.api.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public final class Mailbox {
    private static final int BATCH_SIZE = 1024;

    private final String name;

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicInteger depth = new AtomicInteger();

    private final LongAdder posted = new LongAdder();
    private final LongAdder executed = new LongAdder();
    private final LongAdder failed = new LongAdder();

    @Setter
    private volatile Consumer<Throwable> errorHandler;

    private final Logger log;

    private volatile int maxDepthObserved = 0;

    public boolean post(@NotNull Runnable runnable) {
        if (!accepting.get()) { return false; }
        queue.add(runnable);
        posted.increment();
        int newDepth = depth.incrementAndGet();
        if (newDepth > maxDepthObserved) {
            maxDepthObserved = newDepth;
        }
        return true;
    }

    public void postOrThrow(@NotNull Runnable runnable) {
        if (!post(runnable)) {
            throw new IllegalStateException("Mailbox '" + name + "' is not accepting further tasks! Missing '" + runnable.getClass().getSimpleName() + "'...");
        }
    }

    public void drain() {
        drain(Integer.MAX_VALUE);
    }

    public void drain(int maxTasks) {
        int taskCount = 0;
        for (Runnable runnable; taskCount < maxTasks && (runnable = queue.poll()) != null; taskCount++) {
            depth.decrementAndGet();
            try {
                runnable.run();
                executed.increment();
            } catch (Throwable t) {
                failed.increment();
                final Consumer<Throwable> finalErrorHandler = errorHandler;
                if (errorHandler != null) { errorHandler.accept(t); }
                else { log.error(t.getMessage(), t); }
            }
        }
    }

    public void drainUntilEmpty() {
        drainUntilEmpty(Integer.MAX_VALUE);
    }

    public void drainUntilEmpty(int maxTotalTasks) {
        int processed = 0;
        while (!queue.isEmpty() && processed < maxTotalTasks) {
            int before = executed.intValue();
            drain(BATCH_SIZE);
            processed += (executed.intValue() - before);
        }
    }

    public void shutdown() {
        accepting.set(false);
    }

    public void shutdownAndDrainAll() {
        shutdown();
        drainUntilEmpty();
    }

    public boolean isEmpty() { return queue.isEmpty(); }

    public int depth() { return depth.get(); }
    public long postedCount() { return posted.sum(); }
    public long executedCount() { return executed.sum(); }
    public long failedCount() { return failed.sum(); }
    public int maxDepthObserved() { return maxDepthObserved; }
    public boolean isAccepting() { return accepting.get(); }
}