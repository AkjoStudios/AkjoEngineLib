package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.res.EngineResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface AssetManager {
    /**
     * Synchronously retrieves an asset. If not loaded, it blocks the current thread until loaded.
     * @apiNote Not recommended for gameplay, only useful for initialization or loading screens.
     */
    @NotNull <T extends Asset> T load(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Synchronously retrieves an asset. If not loaded, it blocks the current thread until loaded.
     * @apiNote Not recommended for gameplay, only useful for initialization or loading screens.
     */
    @NotNull <T extends Asset> T load(@NotNull EngineResources<T> resource);

    /**
     * Asynchronously loads an asset.
     * @return A future that completes when the asset is ready.
     */
    @NotNull <T extends Asset>CompletableFuture<T> loadAsync(@NotNull ResourcePath path, @NotNull Class<T> type);

    /**
     * Asynchronously loads an asset.
     * @return A future that completes when the asset is ready.
     */
    @NotNull <T extends Asset>CompletableFuture<T> loadAsync(@NotNull EngineResources<T> resource);

    /**
     * @return The asset if it is already loaded and cached, otherwise null.
     */
    @Nullable <T extends Asset> T get(@NotNull ResourcePath path);

    /**
     * Unloads the asset and disposes it (freeing memory).
     */
    void unload(@NotNull ResourcePath path);
}