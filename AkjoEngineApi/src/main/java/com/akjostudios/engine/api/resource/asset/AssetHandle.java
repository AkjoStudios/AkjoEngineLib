package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetHandle<T extends Asset> {
    @NotNull ResourcePath path();
    @NotNull Class<T> type();

    @Nullable T get();
    default boolean isReady() {
        return get() != null;
    }
    default void ifReady(@NotNull Consumer<T> consumer) {
        T value = get();
        if (value != null) consumer.accept(value);
    }

    @NotNull CompletableFuture<T> preload();
}