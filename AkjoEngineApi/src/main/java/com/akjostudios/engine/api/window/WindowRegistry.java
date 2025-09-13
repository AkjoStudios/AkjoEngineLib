package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.window.builder.WindowBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowRegistry {
    /**
     * @return A window builder for the given parameters.
     */
    <T extends WindowBuilder> T builder(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull Monitor monitor,
            boolean vsync
    );

    /**
     * @apiNote This method does not work in the initialization phase.
     * @return A list of all registered windows.
     */
    @NotNull List<Window> getWindows();
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return The monitor with the given id/handle.
     */
    @NotNull Monitor getMonitorById(long id);
}