package com.akjostudios.engine.api.render.context;

import com.akjostudios.engine.api.window.FramebufferResolution;
import org.jetbrains.annotations.NotNull;

public record FrameInfo(
        @NotNull FramebufferResolution resolution
) {}