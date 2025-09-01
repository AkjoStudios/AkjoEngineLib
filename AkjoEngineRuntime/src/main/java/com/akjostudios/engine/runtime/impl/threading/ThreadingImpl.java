package com.akjostudios.engine.runtime.impl.threading;

import com.akjostudios.engine.api.common.Mailbox;
import com.akjostudios.engine.api.common.Waiter;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.threading.Threading;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class ThreadingImpl implements Threading {
    private static final String DEFAULT_MAIN_THREAD_NAME = "main";

    private static final String MAIN_THREAD_NAME = "Runtime";
    private static final String RENDER_THREAD_NAME = "Render";
    private static final String LOGIC_THREAD_NAME = "Logic";
    private static final String AUDIO_THREAD_NAME = "Audio";
    private static final String WORKER_THREAD_PREFIX = "Worker";

    private static final int MAILBOX_DRAIN_SIZE = 1024;
    private static final long MAILBOX_EMPTY_PARK_TIME_NS = 1_000_000L;
    private static final int THREAD_JOIN_TIMEOUT_MS = 5000;

    private static final ThreadLocal<Boolean> IS_RENDER = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_LOGIC = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_AUDIO = ThreadLocal.withInitial(() -> false);

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    private Logger log;

    private volatile Thread.UncaughtExceptionHandler exceptionHandler;

    // Render thread
    private volatile Thread renderThread;
    private final AtomicBoolean renderRunning = new AtomicBoolean(false);
    private final Waiter renderWaiter = new Waiter();
    private final Mailbox renderMailbox;

    // Logic thread
    private volatile Thread logicThread;
    private final AtomicBoolean logicRunning = new AtomicBoolean(false);
    private double logicStepSeconds = 1.0 / 60.0;
    private volatile LogicCallback logicCallback;

    // Audio thread
    private volatile Thread audioThread;
    private final AtomicBoolean audioRunning = new AtomicBoolean(false);
    private final Waiter audioWaiter = new Waiter();
    private final Mailbox audioMailbox;

    // Worker threads
    private ExecutorService workerPool;
    private final AtomicInteger workerId = new AtomicInteger(1);

    @Override
    public void runOnRender(@NotNull Runnable task) {
        ensureInitialized();
        renderMailbox.post(task);
        renderWaiter.wake();
    }

    @Override
    public void runOnAudio(@NotNull Runnable task) {
        ensureInitialized();
        audioMailbox.post(task);
        audioWaiter.wake();
    }

    @Override
    public void runOnWorker(@NotNull Runnable task) {
        ensureWorkers();
        workerPool.submit(task);
    }

    @Override
    public <T> @NotNull CompletableFuture<T> runOnWorker(@NotNull Callable<T> task) {
        ensureWorkers();
        return CompletableFuture.supplyAsync(() -> {
            try { return task.call(); } catch (Exception e) { throw new RuntimeException(e); }
        }, workerPool);
    }

    @Override
    public boolean isRenderThread() { return Boolean.TRUE.equals(IS_RENDER.get()); }

    @Override
    public boolean isLogicThread() { return Boolean.TRUE.equals(IS_LOGIC.get()); }

    @Override
    public boolean isAudioThread() { return Boolean.TRUE.equals(IS_AUDIO.get()); }

    @Override
    public void __engine_init(
            @NotNull Object token,
            @NotNull Thread.UncaughtExceptionHandler handler,
            @NotNull Logger logger
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), DEFAULT_MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Threading is not being initialized on main thread!");
        }
        if (!initialized.compareAndSet(false, true)) { return; }

        log = logger;

        Thread current = Thread.currentThread();
        current.setName(MAIN_THREAD_NAME);

        Thread.setDefaultUncaughtExceptionHandler(handler);
        this.exceptionHandler = handler;

        // Setup error handlers of mailboxes
        renderMailbox.errorHandler(this::handleUncaught);
        audioMailbox.errorHandler(this::handleUncaught);
    }

    @Override
    public void __engine_start(
            @NotNull Object token,
            @NotNull Config config,
            @NotNull LogicCallback logicCallback
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        ensureInitialized();
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Threading is not being started on runtime thread!");
        }
        if (!started.compareAndSet(false, true)) { return; }

        this.logicStepSeconds = 1.0 / Math.max(1.0, config.logicHz());
        this.logicCallback = logicCallback;

        // Initialize worker thread pool
        int threads = Math.max(1, config.workerThreads());
        this.workerPool = Executors.newFixedThreadPool(
                threads,
                runnable -> {
                    Thread thread = new Thread(runnable, WORKER_THREAD_PREFIX + workerId.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(exceptionHandler);
                    return thread;
                }
        );

        // Start render thread
        renderRunning.set(true);
        renderThread = new Thread(this::renderLoop, RENDER_THREAD_NAME);
        renderThread.setUncaughtExceptionHandler(exceptionHandler);
        renderThread.start();

        // Start logic thread
        logicRunning.set(true);
        logicThread = new Thread(this::logicLoop, LOGIC_THREAD_NAME);
        logicThread.setUncaughtExceptionHandler(exceptionHandler);
        logicThread.start();

        // Start audio thread
        audioRunning.set(true);
        audioThread = new Thread(this::audioLoop, AUDIO_THREAD_NAME);
        audioThread.setDaemon(true);
        audioThread.setUncaughtExceptionHandler(exceptionHandler);
        audioThread.start();
    }

    @Override
    public void __engine_stop(
            @NotNull Object token
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        if (!started.get() || stopping.getAndSet(true)) { return; }

        // Stop audio thread
        audioRunning.set(false);
        audioWaiter.wake();
        join(audioThread);

        // Stop logic thread
        logicRunning.set(false);
        join(logicThread);

        // Stop render thread
        renderRunning.set(false);
        renderWaiter.wake();
        join(renderThread);

        // Stop all worker threads
        if (workerPool != null) {
            workerPool.shutdown();
            try {
                if (!workerPool.awaitTermination(THREAD_JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    log.warn("⚠️ Worker threads did not terminate in time - some tasks may not have been completed!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                workerPool.shutdownNow();
            }
        }
    }

    private void renderLoop() {
        IS_RENDER.set(true);
        try {
            while (renderRunning.get()) {
                renderMailbox.drain(MAILBOX_DRAIN_SIZE);

                if (renderMailbox.isEmpty()) {
                    renderWaiter.park(MAILBOX_EMPTY_PARK_TIME_NS);
                }
            }
        } finally {
            renderMailbox.shutdownAndDrainAll();
            IS_RENDER.remove();
        }
    }

    private void logicLoop() {
        IS_LOGIC.set(true);
        try {
            final long stepNanos = (long) (logicStepSeconds * 1_000_000_000L);
            long prev = System.nanoTime();
            long acc = 0L;
            while (logicRunning.get()) {
                long now = System.nanoTime();
                acc += now - prev;
                prev = now;

                while (acc >= stepNanos && logicRunning.get()) {
                    try {
                        if (logicCallback != null) {
                            logicCallback.onUpdate(logicStepSeconds);
                        }
                    } catch (Throwable t) {
                        handleUncaught(t);
                    }
                    acc -= stepNanos;
                }

                Thread.onSpinWait();
            }
        } finally {
            IS_LOGIC.remove();
        }
    }

    private void audioLoop() {
        IS_AUDIO.set(true);
        try {
            while (audioRunning.get()) {
                audioMailbox.drain(MAILBOX_DRAIN_SIZE);

                if (audioMailbox.isEmpty()) {
                    audioWaiter.park(MAILBOX_EMPTY_PARK_TIME_NS);
                }
            }
        } finally {
            audioMailbox.shutdownAndDrainAll();
            IS_AUDIO.remove();
        }
    }

    private void handleUncaught(Throwable t) {
        if (exceptionHandler == null) { log.error(t, "Uncaught exception in thread '{}':", Thread.currentThread().getName()); }
        else { exceptionHandler.uncaughtException(Thread.currentThread(), t); }
    }

    private void join(@NotNull Thread thread) {
        try {
            thread.join(ThreadingImpl.THREAD_JOIN_TIMEOUT_MS);
            if (thread.isAlive()) {
                log.warn("⚠️ Thread '{}' did not terminate in time - some tasks may not have been completed!", thread.getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void ensureInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException("❗ Threading system has not been initialized! This is likely a bug in the engine - please report it using the issue tracker.");
        }
    }

    private void ensureWorkers() {
        if (workerPool == null) {
            throw new IllegalStateException("❗ Worker pool has not been initialized yet! Please do not supply worker threads to the threading system until it has been started.");
        }
    }
}