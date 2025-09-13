package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.window.builder.BorderlessWindowBuilder;
import com.akjostudios.engine.api.window.builder.FullscreenWindowBuilder;
import com.akjostudios.engine.api.window.builder.WindowBuilder;
import com.akjostudios.engine.api.window.builder.WindowedWindowBuilder;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowMode<T extends WindowBuilder> {
    @NotNull Class<T> provide();

    WindowMode<WindowedWindowBuilder> WINDOWED = () -> WindowedWindowBuilder.class;
    WindowMode<BorderlessWindowBuilder> BORDERLESS = () -> BorderlessWindowBuilder.class;
    WindowMode<FullscreenWindowBuilder> FULLSCREEN = () -> FullscreenWindowBuilder.class;
}