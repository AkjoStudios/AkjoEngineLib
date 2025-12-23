package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.res.EngineResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetManager {
    /**
     * Synchronously retrieves an asset. If not loaded, it blocks the current thread until loaded.
     * @apiNote Not recommended for gameplay, only useful for initialization or loading screens.
     */
    <T extends Asset> @NotNull T load(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Synchronously retrieves an asset by the engine. If not loaded, it blocks the current thread until loaded.
     * @apiNote Not recommended for gameplay, only useful for initialization or loading screens.
     */
    <T extends Asset> @NotNull T load(@NotNull EngineResources<T> resource);

    /**
     * Asynchronously loads an asset.
     * @return A future that completes when the asset is ready.
     */
    <T extends Asset> @NotNull CompletableFuture<T> loadAsync(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Asynchronously loads an asset provided by the engine.
     * @return A future that completes when the asset is ready.
     */
    <T extends Asset> @NotNull CompletableFuture<T> loadAsync(@NotNull EngineResources<T> resource);

    /**
     * Creates an asset handle for retrieving an asset.
     */
    <T extends Asset> @NotNull AssetHandle<T> handle(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Creates an asset handle for retrieving an asset by the engine.
     */
    default <T extends Asset> @NotNull AssetHandle<T> handle(@NotNull EngineResources<T> resource) {
        return handle(resource.path(), resource.type());
    }

    /**
     * Creates an asset handle for preloading an asset.
     */
    <T extends Asset> @NotNull AssetHandle<T> preloadHandle(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Creates an asset handle for preloading an asset by the engine.
     */
    default <T extends Asset> @NotNull AssetHandle<T> preloadHandle(@NotNull EngineResources<T> resource) {
        return preloadHandle(resource.path(), resource.type());
    }

    /**
     * Preloads a given asset for it to be used later.
     */
    default <T extends Asset> void preload(@NotNull ResourcePath path, @NotNull Class<T> type) {
        loadAsync(path, type);
    }

    /**
     * Preloads a given asset for it to be used later.
     */
    default <T extends Asset> void preload(@NotNull EngineResources<T> resource) {
        loadAsync(resource);
    }

    /**
     * @return The asset if it is already loaded and cached, otherwise null.
     */
    <T extends Asset> @Nullable T get(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * @return The asset if it is already loaded and cached, otherwise null.
     */
    default <T extends Asset> @Nullable T get(@NotNull EngineResources<T> resource) {
        return get(resource.path(), resource.type());
    }

    /**
     * Unloads the asset and disposes it (freeing memory).
     */
    <T extends Asset> void unload(@NotNull ResourcePath path, @NotNull Class<T> type);
}