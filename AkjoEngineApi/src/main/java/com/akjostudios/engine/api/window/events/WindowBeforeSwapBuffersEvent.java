package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.window.Window;
import org.jetbrains.annotations.NotNull;

public record WindowBeforeSwapBuffersEvent(
        @NotNull Window window
) implements Event {}