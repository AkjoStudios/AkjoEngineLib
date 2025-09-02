package com.akjostudios.engine.runtime.impl.threading;

import com.akjostudios.engine.api.common.Mailbox;
import com.akjostudios.engine.api.common.Waiter;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.scheduling.TickScheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.runtime.impl.scheduling.FrameSchedulerImpl;
import com.akjostudios.engine.runtime.impl.scheduling.TickSchedulerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class ThreadingImpl implements Threading {
    public static final String DEFAULT_MAIN_THREAD_NAME = "main";

    public static final String MAIN_THREAD_NAME = "Runtime";
    public static final String RENDER_THREAD_NAME = "Render";
    public static final String LOGIC_THREAD_NAME = "Logic";
    public static final String AUDIO_THREAD_NAME = "Audio";
    public static final String WORKER_THREAD_PREFIX = "Worker";

    private static final int MAILBOX_DRAIN_SIZE = 1024;
    private static final long MAILBOX_EMPTY_PARK_TIME_NS = 1_000_000L;
    private static final int THREAD_JOIN_TIMEOUT_MS = 5000;

    private static final long LOGIC_NANOS_PER_SECOND = 1_000_000_000L;
    private static final int LOGIC_MAX_UPDATES = 5;
    private static final int LOGIC_YIELD_DIVISOR = 2;

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
    @Getter
    private final Mailbox renderMailbox;

    private FrameSchedulerImpl renderScheduler;

    // Logic thread
    private volatile Thread logicThread;
    private final AtomicBoolean logicRunning = new AtomicBoolean(false);
    private volatile double logicStepSeconds = 1.0 / 60.0;
    private volatile LogicCallback logicCallback;

    private final Waiter logicWaiter = new Waiter();
    @Getter
    private final Mailbox logicMailbox;

    private TickSchedulerImpl logicScheduler;

    // Audio thread
    private volatile Thread audioThread;
    private final AtomicBoolean audioRunning = new AtomicBoolean(false);

    private final Waiter audioWaiter = new Waiter();
    @Getter
    private final Mailbox audioMailbox;

    private FrameSchedulerImpl audioScheduler;

    // Worker threads
    private ExecutorService workerPool;
    private final AtomicInteger workerId = new AtomicInteger(1);


    @Override
    public void runOnWorker(@NotNull Runnable task) {
        ensureWorkers();
        if (workerPool.isShutdown()) {
            throw new IllegalStateException("❗ Worker pool has been shutdown! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        workerPool.submit(task);
    }

    @Override
    public <T> @NotNull CompletableFuture<T> runOnWorker(@NotNull Callable<T> task) {
        ensureWorkers();
        if (workerPool.isShutdown()) {
            throw new IllegalStateException("❗ Worker pool has been shutdown! This is likely a bug in the engine - please report it using the issue tracker.");
        }
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

    /**
     * Initializes the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread AND only once.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_init(
            @NotNull Object token,
            @NotNull Thread.UncaughtExceptionHandler handler,
            @NotNull Logger logger
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), DEFAULT_MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Threading is not being initialized on main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        if (!initialized.compareAndSet(false, true)) { return; }

        log = logger;

        Thread current = Thread.currentThread();
        current.setName(MAIN_THREAD_NAME);

        Thread.setDefaultUncaughtExceptionHandler(handler);
        this.exceptionHandler = handler;

        // Setup error handlers of mailboxes
        renderMailbox.errorHandler(this::handleUncaught);
        logicMailbox.errorHandler(this::handleUncaught);
        audioMailbox.errorHandler(this::handleUncaught);
    }

    /**
     * Starts the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread AND only once.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_start(
            @NotNull Object token,
            @NotNull Config config,
            @NotNull LogicCallback logicCallback
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        ensureInitialized();
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Threading is not being started on runtime thread! This is likely a bug in the engine - please report it using the issue tracker.");
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

    /**
     * Stops the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND only once.
     * @throws IllegalCallerException When this method is called externally.
     */
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
        logicWaiter.wake();
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

    /**
     * Sets the render scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a frame scheduler of type FrameSchedulerImpl.
     */
    @Override
    public void __engine_setRenderScheduler(
            @NotNull Object token,
            @NotNull FrameScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Render scheduler must be set on the main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        if (!(scheduler instanceof FrameSchedulerImpl impl)) {
            throw new IllegalArgumentException("❗ Render scheduler must be of type FrameSchedulerImpl! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.renderScheduler = impl;
    }

    /**
     * Sets the logic scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a tick scheduler of type TickSchedulerImpl.
     */
    @Override
    public void __engine_setLogicScheduler(
            @NotNull Object token,
            @NotNull TickScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Logic scheduler must be set on the main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        if (!(scheduler instanceof TickSchedulerImpl impl)) {
            throw new IllegalArgumentException("❗ Logic scheduler must be of type TickSchedulerImpl! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.logicScheduler = impl;
    }

    /**
     * Sets the audio scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a frame scheduler of type FrameSchedulerImpl.
     */
    @Override
    public void __engine_setAudioScheduler(
            @NotNull Object token,
            @NotNull FrameScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Audio scheduler must be set on the main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        if (!(scheduler instanceof FrameSchedulerImpl impl)) {
            throw new IllegalArgumentException("❗ Audio scheduler must be of type FrameSchedulerImpl! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.audioScheduler = impl;
    }

    private void renderLoop() {
        IS_RENDER.set(true);
        try {
            while (renderRunning.get()) {
                renderMailbox.drain(MAILBOX_DRAIN_SIZE);

                if (renderScheduler != null) {
                    renderScheduler.onFrame();
                }

                if (renderMailbox.isEmpty()) {
                    renderWaiter.park(MAILBOX_EMPTY_PARK_TIME_NS);
                }
            }
        } catch (Exception e) {
            log.error("⚠️ Render thread encountered an exception - shutting down rendering thread!");
        } finally {
            renderMailbox.shutdownAndDrainAll();
            IS_RENDER.remove();
        }
    }

    private void logicLoop() {
        IS_LOGIC.set(true);
        try {
            final long stepNanos = (long) (logicStepSeconds * LOGIC_NANOS_PER_SECOND);
            long lastTime = System.nanoTime();
            long accumulator = 0L;
            long maxAccumulator = stepNanos * LOGIC_MAX_UPDATES;

            while (logicRunning.get() && !Thread.currentThread().isInterrupted()) {
                logicMailbox.drain(MAILBOX_DRAIN_SIZE);

                long currentTime = System.nanoTime();
                long frameTime = currentTime - lastTime;
                lastTime = currentTime;

                frameTime = Math.min(frameTime, stepNanos * LOGIC_MAX_UPDATES);
                accumulator += frameTime;

                accumulator = Math.min(accumulator, maxAccumulator);

                boolean didUpdate = false;
                int updateCount = 0;

                while (accumulator >= stepNanos && logicRunning.get() && updateCount < LOGIC_MAX_UPDATES) {
                    try {
                        if (logicCallback != null) {
                            logicCallback.onUpdate(logicStepSeconds);
                            if (logicScheduler != null) {
                                logicScheduler.onTick();
                            }
                            didUpdate = true;
                            updateCount++;
                        }
                    } catch (Throwable t) {
                        handleUncaught(t);
                    }
                    accumulator -= stepNanos;
                }

                if (!didUpdate && logicMailbox.isEmpty()) {
                    logicWaiter.park(MAILBOX_EMPTY_PARK_TIME_NS);
                } else if (accumulator < stepNanos / LOGIC_YIELD_DIVISOR) {
                    Thread.yield();
                } else {
                    Thread.onSpinWait();
                }
            }
        } catch (Exception e) {
            log.error("⚠️ Logic thread encountered an exception - shutting down logic thread!");
        } finally {
            logicMailbox.shutdownAndDrainAll();
            IS_LOGIC.remove();
        }
    }

    private void audioLoop() {
        IS_AUDIO.set(true);
        try {
            while (audioRunning.get()) {
                audioMailbox.drain(MAILBOX_DRAIN_SIZE);

                if (audioScheduler != null) {
                    audioScheduler.onFrame();
                }

                if (audioMailbox.isEmpty()) {
                    audioWaiter.park(MAILBOX_EMPTY_PARK_TIME_NS);
                }
            }
        } catch (Exception e) {
            log.error("⚠️ Audio thread encountered an exception - shutting down audio thread!");
        } finally {
            audioMailbox.shutdownAndDrainAll();
            IS_AUDIO.remove();
        }
    }

    private void handleUncaught(Throwable t) {
        try {
            if (exceptionHandler == null) {
                log.error(t, "❗ Uncaught exception in thread '{}':", Thread.currentThread().getName());
            } else {
                exceptionHandler.uncaughtException(Thread.currentThread(), t);
            }
        } catch (Throwable handlerException) {
            System.err.println("Exception while handling uncaught exception in thread '" + Thread.currentThread().getName() + "':");
            //noinspection CallToPrintStackTrace
            t.printStackTrace();
        }
    }

    private void join(@NotNull Thread thread) {
        try {
            thread.join(ThreadingImpl.THREAD_JOIN_TIMEOUT_MS);
            if (thread.isAlive()) {
                log.warn("⚠️ Thread '{}' did not terminate in time - interrupting it", thread.getName());
                thread.interrupt();
                thread.join(ThreadingImpl.THREAD_JOIN_TIMEOUT_MS);
                if (thread.isAlive()) {
                    log.error("❗ Thread '{}' is still alive after interrupting it - possible resource leak!", thread.getName());
                }
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