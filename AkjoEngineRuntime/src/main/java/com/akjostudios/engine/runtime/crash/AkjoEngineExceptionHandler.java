package com.akjostudios.engine.runtime.crash;

import com.akjostudios.engine.api.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@RequiredArgsConstructor
public final class AkjoEngineExceptionHandler implements Thread.UncaughtExceptionHandler{
    @NotNull private final Logger logger;
    @NotNull private final String appName;
    @NotNull private final String appVersion;
    @NotNull private final String engineVersion;
    @NotNull private final Consumer<Thread> shutdownSignal;

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String role = deriveRole(thread);
        var info = new CrashReportFormatter.EngineContextInfo(
                appName, appVersion, engineVersion, role
        );
        String report = CrashReportFormatter.format(throwable, thread, info);

        try {
            logger.error(report);
        } catch (Throwable ignored) {
            System.err.println(report);
        }

        if (throwable instanceof RuntimeException) { return; }
        safeShutdown(thread);
    }

    private void safeShutdown(@NotNull Thread thread) {
        try { shutdownSignal.accept(thread); } catch (Exception ignored) {}
    }

    private static String deriveRole(@NotNull Thread thread) {
        String name = thread.getName();
        if (name.startsWith("Runtime") || name.startsWith("main")) { return "runtime"; }
        if (name.startsWith("Render")) { return "render"; }
        if (name.startsWith("Logic")) { return "logic"; }
        if (name.startsWith("Audio")) { return "audio"; }
        if (name.startsWith("Worker")) { return "worker"; }
        return "unknown";
    }
}