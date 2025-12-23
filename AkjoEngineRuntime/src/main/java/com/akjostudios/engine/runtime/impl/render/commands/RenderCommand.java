package com.akjostudios.engine.runtime.impl.render.commands;

import org.jetbrains.annotations.NotNull;

public sealed interface RenderCommand permits ClearCommand {
    void execute();

    @FunctionalInterface
    interface Sink {
        void accept(@NotNull RenderCommand command);
    }
}