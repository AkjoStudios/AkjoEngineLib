package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowResolution;
import org.jetbrains.annotations.NotNull;

public record WindowResizedEvent(
        @NotNull Window window,
        @NotNull WindowResolution oldResolution
) implements Event {}