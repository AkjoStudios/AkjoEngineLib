package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowOptions;
import org.jetbrains.annotations.NotNull;

public record WindowOptionsChangedEvent(
        @NotNull Window window,
        @NotNull WindowOptions oldOptions
) implements Event {}