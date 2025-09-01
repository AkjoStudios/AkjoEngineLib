package com.akjostudios.engine.runtime;

import com.akjostudios.engine.api.AkjoApplication;
import com.akjostudios.engine.api.IAkjoApplication;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.runtime.crash.AkjoEngineExceptionHandler;
import com.akjostudios.engine.runtime.impl.AkjoApplicationContext;
import com.akjostudios.engine.runtime.impl.lifecycle.LifecycleImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class AkjoEngineRuntime implements SmartLifecycle {
    private static final String RUNTIME_LOGGER_NAME = "engine.runtime";
    private static final String CRASH_LOGGER_NAME = "engine.crash";
    private static final String THREADING_LOGGER_NAME = "engine.threading";

    private static final String APP_NAME_PROPERTY = "spring.application.name";
    private static final String ENGINE_VERSION_PROPERTY = "engine.version";

    private static final String LIFECYCLE_THREAD_NAME = "Lifecycle";

    private final IAkjoApplication application;
    private final AkjoApplicationContext context;
    private final AkjoEngineAppProperties properties;
    private final Map<String, Object> systemProperties;

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

            // Initialize threading system
            context.threading().__engine_init(
                    EngineTokens.token(),
                    new AkjoEngineExceptionHandler(
                            context.logger(CRASH_LOGGER_NAME),
                            properties.appName(),
                            properties.appVersion(),
                            systemProperties.get(ENGINE_VERSION_PROPERTY).toString(),
                            this::stop
                    ),
                    context.logger(THREADING_LOGGER_NAME)
            );

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

    private void safeDestroy() {
        if (shutdown.get()) { return; }
        try { application.onDestroy(); } catch (Exception ignored) {}
        shutdown.set(true);
        log.info("💤 Application has been stopped and engine has shut down gracefully. Goodbye!");
    }
}