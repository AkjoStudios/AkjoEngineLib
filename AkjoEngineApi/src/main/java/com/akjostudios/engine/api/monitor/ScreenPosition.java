package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.position.IPosition2D;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record ScreenPosition(long x, long y) implements IPosition2D {}