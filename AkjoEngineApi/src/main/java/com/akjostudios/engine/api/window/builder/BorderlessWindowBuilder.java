package com.akjostudios.engine.api.window.builder;

import com.akjostudios.engine.api.window.WindowVisibility;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface BorderlessWindowBuilder extends WindowBuilder {
    /**
     * Sets the initial visibility of the borderless window.
     */
    @NotNull BorderlessWindowBuilder visibility(@NotNull WindowVisibility visibility);
    /**
     * Sets the initial value of the resizable option for this window.
     */
    @NotNull BorderlessWindowBuilder resizable(boolean resizable);
}