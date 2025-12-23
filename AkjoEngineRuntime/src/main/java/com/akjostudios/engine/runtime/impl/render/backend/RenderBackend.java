package com.akjostudios.engine.runtime.impl.render.backend;

import com.akjostudios.engine.runtime.impl.render.commands.RenderCommand;
import org.jetbrains.annotations.NotNull;

public sealed interface RenderBackend permits CanvasRenderBackend {
    void execute(@NotNull RenderCommand command);
    void flush();
}