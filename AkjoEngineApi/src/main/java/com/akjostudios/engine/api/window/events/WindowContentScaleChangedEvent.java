package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowContentScale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record WindowContentScaleChangedEvent(
        @NotNull Window window,
        @Nullable WindowContentScale oldScale
) implements Event {}