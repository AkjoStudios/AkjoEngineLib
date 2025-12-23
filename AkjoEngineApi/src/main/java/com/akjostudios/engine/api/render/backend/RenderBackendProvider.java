package com.akjostudios.engine.api.render.backend;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface RenderBackendProvider {
    @NotNull RenderBackend retrieve();
}