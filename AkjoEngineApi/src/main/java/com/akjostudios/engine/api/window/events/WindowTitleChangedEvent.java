package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.window.Window;
import org.jetbrains.annotations.NotNull;

public record WindowTitleChangedEvent(
        @NotNull Window window,
        @NotNull String oldTitle
) implements Event {}