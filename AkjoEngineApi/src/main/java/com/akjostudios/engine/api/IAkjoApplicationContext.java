package com.akjostudios.engine.api;

import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.threading.Threading;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface IAkjoApplicationContext {
    // Main APIs
    @NotNull Lifecycle lifecycle();
    @NotNull Threading threading();

    // Logging
    @NotNull Logger logger();
    @NotNull Logger logger(@NotNull String name);
    default @NotNull Logger logger(@NotNull Class<?> type) {
        return logger(type.getSimpleName());
    }

    /**
     * Sets the internal lifecycle object for this application.
     * @apiNote Should only be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    void __engine_setLifecycle(
            @NotNull Object token,
            @NotNull Lifecycle lifecycle
    ) throws IllegalCallerException;
}