package com.akjostudios.engine.api.resource.asset;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetHandle<T> extends AutoCloseable {
    @NotNull CompletableFuture<T> whenLoaded();
    @NotNull Optional<T> getWhenLoaded();
    boolean isLoaded();
    void onReload(@NotNull Runnable listener);
    @Override void close();
}