package com.akjostudios.engine.runtime.impl;

import com.akjostudios.engine.api.IAkjoApplicationContext;
import com.akjostudios.engine.api.common.Mailbox;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.runtime.impl.logging.LoggerImpl;
import com.akjostudios.engine.runtime.impl.threading.ThreadingImpl;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public final class AkjoApplicationContext implements IAkjoApplicationContext {
    private static final String DEFAULT_BASE_LOGGER_NAME = "app";

    private final Threading threading = new ThreadingImpl(
            new Mailbox("render", logger("engine.mailbox.render")),
            new Mailbox("audio", logger("engine.mailbox.audio"))
    );

    @Setter
    private volatile String baseLoggerName = DEFAULT_BASE_LOGGER_NAME;

    private Lifecycle lifecycle;

    @Override
    public @NotNull Logger logger() {
        String base = this.baseLoggerName;
        if (base == null || base.isBlank()) base = DEFAULT_BASE_LOGGER_NAME;
        return new LoggerImpl(base);
    }

    @Override
    public @NotNull Logger logger(@NotNull String name) {
        String base = this.baseLoggerName;
        if (base == null || base.isBlank()) { base = "app"; }
        String fullName = name.startsWith(base) ? name : base + " / " + name;
        return new LoggerImpl(fullName);
    }

    @Override
    public @NotNull Lifecycle lifecycle() {
        if (lifecycle == null) {
            throw new IllegalStateException("❗ No lifecycle object has been set! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return lifecycle;
    }

    @Override
    public @NotNull Threading threading() { return threading; }

    /**
     * Sets the internal lifecycle object for this application.
     * @apiNote Should only be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    @Override
    public void __engine_setLifecycle(
            @NotNull Object token,
            @NotNull Lifecycle lifecycle
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        this.lifecycle = lifecycle;
    }
}