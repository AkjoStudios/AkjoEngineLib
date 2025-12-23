package com.akjostudios.engine.api.render.command;

import org.jetbrains.annotations.NotNull;

public interface RenderCommand {
    @FunctionalInterface
    interface Sink {
        void accept(@NotNull RenderCommand command) throws Exception;
    }
}