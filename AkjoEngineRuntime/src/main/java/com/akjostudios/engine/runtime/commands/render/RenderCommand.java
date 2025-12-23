package com.akjostudios.engine.runtime.commands.render;

import org.jetbrains.annotations.NotNull;

public sealed interface RenderCommand permits ClearCommand {
    void execute();

    @FunctionalInterface
    interface Sink {
        void accept(@NotNull RenderCommand command);
    }
}