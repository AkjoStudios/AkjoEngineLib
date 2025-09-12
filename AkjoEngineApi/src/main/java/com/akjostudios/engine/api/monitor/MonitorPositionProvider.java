package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MonitorPositionProvider {
    @NotNull MonitorPosition retrieve(@NotNull Monitor monitor, @NotNull IResolution object);
}