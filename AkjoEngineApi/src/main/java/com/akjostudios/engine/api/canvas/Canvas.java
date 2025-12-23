package com.akjostudios.engine.api.canvas;

import com.akjostudios.engine.api.common.base.color.IColor;
import org.jetbrains.annotations.NotNull;

public interface Canvas {
    void clear(@NotNull IColor color);
}