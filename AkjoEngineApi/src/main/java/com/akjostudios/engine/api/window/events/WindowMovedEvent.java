package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.window.Window;
import org.jetbrains.annotations.NotNull;

public record WindowMovedEvent(
        @NotNull Window window,
        @NotNull ScreenPosition oldPosition,
        @NotNull MonitorPosition oldMonitorPosition
) implements Event {}