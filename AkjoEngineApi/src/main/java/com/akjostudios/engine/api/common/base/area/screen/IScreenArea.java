package com.akjostudios.engine.api.common.base.area.screen;

import com.akjostudios.engine.api.common.base.position.HasPosition2D;
import com.akjostudios.engine.api.common.base.position.IPosition2D;
import com.akjostudios.engine.api.common.base.resolution.HasResolution;
import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface IScreenArea extends HasPosition2D, HasResolution {
    @NotNull IPosition2D position();
    @NotNull IResolution resolution();
}