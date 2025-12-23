package com.akjostudios.engine.runtime.impl.render.command;

import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.api.render.command.RenderCommand;
import org.jetbrains.annotations.NotNull;

public record ClearCommand(
        @NotNull IColor color
) implements RenderCommand {}