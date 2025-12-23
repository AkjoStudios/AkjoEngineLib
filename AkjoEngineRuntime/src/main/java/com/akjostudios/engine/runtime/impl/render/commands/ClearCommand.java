package com.akjostudios.engine.runtime.impl.render.commands;

import com.akjostudios.engine.api.common.base.color.IColor;
import org.jetbrains.annotations.NotNull;

public record ClearCommand(
        @NotNull IColor color
) implements RenderCommand {}