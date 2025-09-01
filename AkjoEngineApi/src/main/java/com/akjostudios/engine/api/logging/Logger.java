package com.akjostudios.engine.api.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface Logger {
    void trace(@NotNull String message);
    void debug(@NotNull String message);
    void info(@NotNull String message);
    void warn(@NotNull String message);
    void error(@NotNull String message);

    void trace(@NotNull Supplier<String> message);
    void debug(@NotNull Supplier<String> message);
    void info(@NotNull Supplier<String> message);
    void warn(@NotNull Supplier<String> message);
    void error(@NotNull Supplier<String> message);

    void warn(@NotNull Throwable throwable);
    void error(@NotNull Throwable throwable);

    void warn(@NotNull String message, @NotNull Throwable throwable);
    void error(@NotNull String message, @NotNull Throwable throwable);

    void trace(@NotNull String template, @Nullable Object... values);
    void debug(@NotNull String template, @Nullable Object... values);
    void info(@NotNull String template, @Nullable Object... values);
    void warn(@NotNull String template, @Nullable Object... values);
    void error(@NotNull String template, @Nullable Object... values);

    void warn(@NotNull Throwable throwable, @NotNull String template, @Nullable Object... values);
    void error(@NotNull Throwable throwable, @NotNull String template, @Nullable Object... values);

    boolean isTraceEnabled();
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isWarnEnabled();
    boolean isErrorEnabled();
}