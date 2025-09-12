package com.akjostudios.engine.api.common.base.area.screen;

import com.akjostudios.engine.api.common.base.position.IPosition2D;
import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface IScreenArea {
    @NotNull IPosition2D position();
    @NotNull IResolution resolution();
}