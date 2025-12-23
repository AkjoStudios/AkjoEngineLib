package com.akjostudios.engine.runtime.impl.resource.asset;

import com.akjostudios.engine.api.resource.asset.Asset;
import com.akjostudios.engine.api.resource.asset.AssetHandle;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Accessors(fluent = true)
public final class AssetHandleImpl<T extends Asset> implements AssetHandle<T> {
    private final AssetManager assets;

    @Getter private final ResourcePath path;
    @Getter private final Class<T> type;

    private final AtomicReference<CompletableFuture<T>> preloadFuture = new AtomicReference<>();

    @Override
    public @Nullable T get() {
        T asset = assets.get(path, type);
        if (asset == null) { return null; }

        if (!type.isInstance(asset)) {
            throw new IllegalArgumentException("❗ Asset at \"" + path + "\" is not of type \"" + type.getSimpleName() + "\"!");
        }

        return asset;
    }

    @Override
    public @NotNull CompletableFuture<T> preload() {
        T ready = get();
        if (ready != null) {
            return CompletableFuture.completedFuture(ready);
        }

        CompletableFuture<T> existing = preloadFuture.get();
        if (existing != null) { return existing; }

        CompletableFuture<T> created = assets.loadAsync(path, type);
        if (preloadFuture.compareAndSet(null, created)) {
            return created;
        }

        return Objects.requireNonNull(preloadFuture.get());
    }
}