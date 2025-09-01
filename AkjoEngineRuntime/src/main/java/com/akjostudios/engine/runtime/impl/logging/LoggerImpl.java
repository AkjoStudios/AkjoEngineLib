package com.akjostudios.engine.runtime.impl.logging;

import com.akjostudios.engine.api.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class LoggerImpl implements Logger {
    private final org.slf4j.Logger delegate;

    public LoggerImpl(@NotNull String name) {
        this.delegate = LoggerFactory.getLogger(name);
    }

    @Override
    public void trace(@NotNull String message) { delegate.trace(message); }

    @Override
    public void debug(@NotNull String message) { delegate.debug(message); }

    @Override
    public void info(@NotNull String message) { delegate.info(message); }

    @Override
    public void warn(@NotNull String message) { delegate.warn(message); }

    @Override
    public void error(@NotNull String message) { delegate.error(message); }

    @Override
    public void trace(@NotNull Supplier<String> message) { delegate.trace(message.get()); }

    @Override
    public void debug(@NotNull Supplier<String> message) { delegate.debug(message.get()); }

    @Override
    public void info(@NotNull Supplier<String> message) { delegate.info(message.get()); }

    @Override
    public void warn(@NotNull Supplier<String> message) { delegate.warn(message.get()); }

    @Override
    public void error(@NotNull Supplier<String> message) { delegate.error(message.get()); }

    @Override
    public void warn(@NotNull Throwable throwable) { delegate.warn("", throwable);}

    @Override
    public void error(@NotNull Throwable throwable) { delegate.error("", throwable);}

    @Override
    public void warn(@NotNull String message, @NotNull Throwable throwable) { delegate.warn(message, throwable); }

    @Override
    public void error(@NotNull String message, @NotNull Throwable throwable) { delegate.error(message, throwable); }

    @Override
    public void trace(@NotNull String template, @Nullable Object... values) { delegate.trace(template, values); }

    @Override
    public void debug(@NotNull String template, @Nullable Object... values) { delegate.debug(template, values); }

    @Override
    public void info(@NotNull String template, @Nullable Object... values) { delegate.info(template, values); }

    @Override
    public void warn(@NotNull String template, @Nullable Object... values) { delegate.warn(template, values); }

    @Override
    public void error(@NotNull String template, @Nullable Object... values) { delegate.error(template, values); }

    @Override
    public void warn(@NotNull Throwable throwable, @NotNull String template, @Nullable Object... values) {
        delegate.warn(template, values);
        delegate.warn("", throwable);
    }

    @Override
    public void error(@NotNull Throwable throwable, @NotNull String template, @Nullable Object... values) {
        delegate.error(template, values);
        delegate.error("", throwable);
    }

    @Override
    public boolean isTraceEnabled() { return delegate.isTraceEnabled(); }

    @Override
    public boolean isDebugEnabled() { return delegate.isDebugEnabled(); }

    @Override
    public boolean isInfoEnabled() { return delegate.isInfoEnabled(); }

    @Override
    public boolean isWarnEnabled() { return delegate.isWarnEnabled(); }

    @Override
    public boolean isErrorEnabled() { return delegate.isErrorEnabled(); }
}