package com.akjostudios.engine.api.monitor.events;

import com.akjostudios.engine.api.event.Event;
import com.akjostudios.engine.api.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

public record MonitorConnectedEvent(@NotNull Monitor monitor) implements Event {}