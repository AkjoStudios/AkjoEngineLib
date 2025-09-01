package com.akjostudios.engine.runtime.crash;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CrashReportFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSSxxx"
    );

    private static final Set<String> OMIT_PREFIXES = Set.of(
            "java.",
            "jdk.internal",
            "sun.reflect",
            "org.springframework",
            "org.slf4j",
            "ch.qos.logback"
    );

    private static final Set<String> ENGINE_PACKAGES = Set.of(
            "com.akjostudios.engine.runtime",
            "com.akjostudios.engine.api"
    );

    private static final String EXCEPTION_CODE = "\033[31;49;1m";
    private static final String HIGHLIGHT_CODE = "\033[39;49;4;1m";
    private static final String RESET_CODE = "\033[0m";

    public static @NotNull String format(
            @NotNull Throwable throwable,
            @NotNull Thread thread,
            @NotNull EngineContextInfo contextInfo
    ) {
        StringBuilder builder = new StringBuilder(2048);
        builder.append("\n========== AkjoEngine Crash Report ==========\n");
        builder.append("Time: ")
                .append(DATE_TIME_FORMATTER.format(OffsetDateTime.now()))
                .append('\n');
        builder.append("App: ")
                .append(contextInfo.appName())
                .append(" v")
                .append(contextInfo.appVersion())
                .append('\n');
        builder.append("Engine: AkjoEngine ")
                .append(contextInfo.engineVersion())
                .append('\n');
        builder.append("Thread: ")
                .append(thread.getName())
                .append("(id=")
                .append(thread.threadId())
                .append(", role=")
                .append(contextInfo.threadRole())
                .append(")\n");
        builder.append("---------------------------------------------\n");
        appendThrowable(builder, throwable, 0);
        builder.append("=============================================");
        return builder.toString();
    }

    private static void appendThrowable(@NotNull StringBuilder builder, @NotNull Throwable throwable, int depth) {
        indent(builder, depth).append(throwable.getClass().getName());
        if (throwable.getMessage() != null) {
            builder.append(": ").append(EXCEPTION_CODE).append(throwable.getMessage()).append(RESET_CODE);
        }
        builder.append('\n');

        for (StackTraceElement element : throwable.getStackTrace()) {
            if (shouldOmit(element)) { continue; }
            indent(builder, depth);
            if (ENGINE_PACKAGES.stream().anyMatch(element.getClassName()::startsWith)) {
                builder.append("  at ").append(element);
            } else {
                builder.append("  ").append(HIGHLIGHT_CODE).append("at ").append(element).append(RESET_CODE);
            }
            builder.append('\n');
        }

        for (Throwable suppressed : throwable.getSuppressed()) {
            indent(builder, depth).append("Suppressed: \n");
            appendThrowable(builder, suppressed, depth + 1);
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            indent(builder, depth).append("Caused by: \n");
            appendThrowable(builder, cause, depth + 1);
        }
    }

    private static boolean shouldOmit(@NotNull StackTraceElement element) {
        return OMIT_PREFIXES.stream().anyMatch(element.getClassName()::startsWith);
    }

    private static @NotNull StringBuilder indent(@NotNull StringBuilder builder, int depth) {
        builder.append("    ".repeat(Math.max(0, depth)));
        return builder;
    }

    public record EngineContextInfo(
            @NotNull String appName,
            @NotNull String appVersion,
            @NotNull String engineVersion,
            @NotNull String threadRole
    ) {}
}