package com.akjostudios.engine.api.window.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.window.Window;
import org.jetbrains.annotations.NotNull;

public record WindowMonitorChangedEvent(
        @NotNull Window window,
        @NotNull Monitor oldMonitor
) implements Event {}