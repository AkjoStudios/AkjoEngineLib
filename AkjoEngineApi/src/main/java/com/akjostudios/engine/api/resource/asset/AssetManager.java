package com.akjostudios.engine.api.resource.asset;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetManager {
    <T> @NotNull AssetHandle<T> load(@NotNull AssetKey<T> key);
    <T> @NotNull Optional<T> getWhenLoaded(@NotNull AssetKey<T> key);
    boolean isLoaded(@NotNull AssetKey<?> key);
    void unload(@NotNull AssetKey<?> key);
    void flush();
}