package com.akjostudios.engine.api.window.builder;

import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowRegistryHook;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowBuilder {
    /**
     * Instantiates the window with the given parameters and registers it in the window registry.
     * @apiNote This method does not work in the initialization phase and must be called from the render thread.
     * @throws IllegalStateException When this method is not called from the render thread.
     * @return The newly created window.
     */
    @NotNull Window build() throws IllegalStateException;

    /**
     * Sets the internal registry hook for the window.
     * @apiNote Must be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    void __engine_setRegistryHook(
            @NotNull Object token,
            @NotNull WindowRegistryHook hook
    ) throws IllegalCallerException;
}