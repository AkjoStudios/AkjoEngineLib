package com.akjostudios.engine.runtime;

import com.akjostudios.engine.api.AkjoApplication;
import com.akjostudios.engine.api.IAkjoApplication;
import com.akjostudios.engine.api.common.Mailbox;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.runtime.components.EventListenerRegistrar;
import com.akjostudios.engine.runtime.crash.AkjoEngineExceptionHandler;
import com.akjostudios.engine.runtime.impl.AkjoApplicationContext;
import com.akjostudios.engine.runtime.impl.event.EventBusImpl;
import com.akjostudios.engine.runtime.impl.lifecycle.LifecycleImpl;
import com.akjostudios.engine.runtime.impl.scheduling.FrameSchedulerImpl;
import com.akjostudios.engine.runtime.impl.scheduling.SchedulerImpl;
import com.akjostudios.engine.runtime.impl.scheduling.TickSchedulerImpl;
import com.akjostudios.engine.runtime.impl.threading.ThreadingImpl;
import com.akjostudios.engine.runtime.impl.time.TimeImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.*;

@RequiredArgsConstructor
public class AkjoEngineRuntime implements SmartLifecycle {
    private static final String RUNTIME_LOGGER_NAME = "engine.runtime";
    private static final String CRASH_LOGGER_NAME = "engine.crash";
    private static final String THREADING_LOGGER_NAME = "engine.threading";
    private static final String EVENT_LOGGER_NAME = "engine.event";

    private static final String APP_NAME_PROPERTY = "spring.application.name";
    private static final String ENGINE_VERSION_PROPERTY = "engine.version";

    private static final String LIFECYCLE_THREAD_NAME = "Lifecycle";

    private final IAkjoApplication application;
    private final AkjoApplicationContext context;
    private final AkjoEngineAppProperties properties;
    private final Map<String, Object> systemProperties;

    private final List<EventListenerRegistrar.Registration> eventListenerRegistrations;

    private Logger log;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final Object lifecycleLock = new Object();

    @Override
    public void start() {
        try {
            // Initialize logging system
            context.setBaseLoggerName(systemProperties.get(APP_NAME_PROPERTY).toString());
            if (application instanceof AkjoApplication base) {
                base.__engine_setLogger(
                        EngineTokens.token(),
                        context.logger(application.getClass())
                );
                log = context.logger(RUNTIME_LOGGER_NAME);
            }

            // Initialize lifecycle for application
            context.__engine_setLifecycle(
                    EngineTokens.token(),
                    new LifecycleImpl(
                            context.logger(RUNTIME_LOGGER_NAME),
                            this::isRunning,
                            createStopHandler()
                    )
            );

            // Initialize context for application
            if (application instanceof AkjoApplication base) {
                base.__engine_setContext(
                        EngineTokens.token(),
                        context
                );
            }

            // Initialize time system
            TimeImpl time = new TimeImpl();
            context.__engine_setTime(EngineTokens.token(), time);

            // Initialize threading system
            Mailbox renderMailbox = new Mailbox(RENDER_THREAD_NAME, context.logger(RENDER_THREAD_NAME));
            Mailbox logicMailbox = new Mailbox(LOGIC_THREAD_NAME, context.logger(LOGIC_THREAD_NAME));
            Mailbox audioMailbox = new Mailbox(AUDIO_THREAD_NAME, context.logger(AUDIO_THREAD_NAME));
            context.__engine_setThreading(
                    EngineTokens.token(),
                    new ThreadingImpl(time, renderMailbox, logicMailbox, audioMailbox)
            );
            context.threading().__engine_init(
                    EngineTokens.token(),
                    new AkjoEngineExceptionHandler(
                            context.logger(CRASH_LOGGER_NAME),
                            properties.appName(),
                            properties.appVersion(),
                            systemProperties.get(ENGINE_VERSION_PROPERTY).toString(),
                            thread -> {
                                thread.interrupt();
                                this.safeStop();
                            }
                    ),
                    context.logger(THREADING_LOGGER_NAME)
            );

            // Initialize scheduling system
            FrameSchedulerImpl renderScheduler = new FrameSchedulerImpl(renderMailbox);
            TickSchedulerImpl logicScheduler = new TickSchedulerImpl(logicMailbox);
            FrameSchedulerImpl audioScheduler = new FrameSchedulerImpl(audioMailbox);
            context.__engine_setScheduler(
                    EngineTokens.token(),
                    new SchedulerImpl(
                            Executors.newScheduledThreadPool(1, runnable -> {
                                Thread thread = new Thread(runnable);
                                thread.setDaemon(true);
                                return thread;
                            }),
                            logicMailbox,
                            renderScheduler, logicScheduler, audioScheduler
                    )
            );
            context.threading().__engine_setRenderScheduler(EngineTokens.token(), renderScheduler);
            context.threading().__engine_setLogicScheduler(EngineTokens.token(), logicScheduler);
            context.threading().__engine_setAudioScheduler(EngineTokens.token(), audioScheduler);

            // Initialize event system
            context.__engine_setEventBus(
                    EngineTokens.token(),
                    new EventBusImpl(
                            context.threading(),
                            context.scheduler(),
                            context.logger(EVENT_LOGGER_NAME)
                    )
            );
            eventListenerRegistrations.forEach(registration -> context.events().subscribe(
                    registration.eventType(),
                    event -> {
                        try {
                            registration.method().invoke(registration.bean(), event);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ));

            // Initialize application
            application.onInit();

            // Start threading system
            context.threading().__engine_start(
                    EngineTokens.token(),
                    new Threading.Config(
                            Math.max(1, Runtime.getRuntime().availableProcessors() - 3),
                            60.0
                    ), deltaTime -> {
                        try { application.onUpdate(deltaTime); }
                        catch (Exception e) { log.error(
                                "❗ Failed to call .onUpdate() on application!"
                        ); }
                    }
            );

            // Start application
            application.onStart();
            running.set(true);
        } catch (Exception e) {
            log.error("❗ Failed to start application!");
            running.set(false);
            safeDestroy();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        synchronized (lifecycleLock) {
            if (!running.get()) { return; }
            Thread.currentThread().setName(LIFECYCLE_THREAD_NAME);
            safeStop();
        }
    }

    @Override
    public boolean isRunning() { return running.get(); }

    @Override
    public boolean isAutoStartup() { return false; }

    private BiConsumer<String, Throwable> createStopHandler() {
        return (reason, cause) -> new Thread(() -> {
                Logger stopLogger = context.logger(RUNTIME_LOGGER_NAME);
                if (cause != null) {
                    stopLogger.error(cause, "⚠️ Stopping application due to: {}", reason);
                } else {
                    stopLogger.info("ℹ️ Stopping application due to: {}", reason);
                }
                try { stop(); } catch (Throwable ignored) {}
        }, LIFECYCLE_THREAD_NAME).start();
    }

    private void safeStop() {
        try {
            // Stop application
            application.onStop();
            context.threading().__engine_stop(EngineTokens.token());
            safeDestroy();
        } catch (Exception e) {
            log.error("❗ Failed to stop application!");
            throw new RuntimeException(e);
        } finally {
            running.set(false);
        }
    }

    private void safeDestroy() {
        if (shutdown.get()) { return; }
        try { application.onDestroy(); } catch (Exception ignored) {}
        shutdown.set(true);
        log.info("💤 Application has been stopped and engine has shut down gracefully. Goodbye!");
    }
}