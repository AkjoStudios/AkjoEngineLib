package com.akjostudios.engine.runtime.impl.resource.asset;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.assets.Text;
import com.akjostudios.engine.api.assets.Texture;
import com.akjostudios.engine.api.common.Disposable;
import com.akjostudios.engine.api.resource.asset.Asset;
import com.akjostudios.engine.api.resource.asset.AssetLoader;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.res.EngineResources;
import com.akjostudios.engine.runtime.impl.assets.shader.ShaderLoader;
import com.akjostudios.engine.runtime.impl.assets.text.TextLoader;
import com.akjostudios.engine.runtime.impl.assets.texture.TextureLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public final class AssetManagerImpl implements AssetManager, Disposable {
    private final FileSystem fs;
    private final Scheduler scheduler;

    private final ExecutorService executor;

    private final Map<ResourcePath, Asset> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, AssetLoader<?, ?>> loaders = new HashMap<>();

    public AssetManagerImpl(
            @NotNull FileSystem fs,
            @NotNull Scheduler scheduler,
            @NotNull Thread.UncaughtExceptionHandler exceptionHandler
    ) {
        this.fs = fs;
        this.scheduler = scheduler;
        this.executor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors()) / 2,
                task -> {
                    Thread thread = new Thread(task, "AssetLoader-Worker");
                    thread.setUncaughtExceptionHandler(exceptionHandler);
                    return thread;
                }
        );
    }

    public void setup() {
        registerLoader(Text.class, new TextLoader());
        registerLoader(Texture.class, new TextureLoader());
        registerLoader(Shader.class, new ShaderLoader());
    }

    private <T extends Asset, D> void registerLoader(@NotNull Class<T> type, @NotNull AssetLoader<T, D> loader) {
        loaders.put(type, loader);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T extends Asset> CompletableFuture<T> loadAsync(@NotNull ResourcePath path, @NotNull Class<T> type) {
        if (cache.containsKey(path)) {
            return CompletableFuture.completedFuture((T) cache.get(path));
        }

        AssetLoader<T, Object> loader = (AssetLoader<T, Object>) loaders.get(type);
        if (loader == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("❗ No loader for asset type \"" + type.getSimpleName() + "\"! This is likely a bug in the engine - please report it using the issue tracker.")
            );
        }

        CompletableFuture<T> future = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                Object intermediate = loader.loadRaw(path, fs);

                scheduler.render().immediate(() -> {
                    try {
                        T asset = loader.createAsset(path, intermediate);
                        cache.put(path, asset);
                        future.complete(asset);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public @NotNull <T extends Asset> CompletableFuture<T> loadAsync(@NotNull EngineResources<T> resource) {
        return loadAsync(resource.path(), resource.type());
    }

    @Override
    public @NotNull <T extends Asset> T load(@NotNull ResourcePath path, @NotNull Class<T> type) {
        try {
            return loadAsync(path, type).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Failed to load asset at path \"" + path + "\"!", e.getCause());
        }
    }

    @Override
    public @NotNull <T extends Asset> T load(@NotNull EngineResources<T> resource) {
        return load(resource.path(), resource.type());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T extends Asset> T get(@NotNull ResourcePath path) {
        return (T) cache.get(path);
    }

    @Override
    public void unload(@NotNull ResourcePath path) {
        Asset asset = cache.remove(path);
        if (asset != null) {
            scheduler.render().immediate(asset::dispose);
        }
    }

    @Override
    public void dispose() {
        executor.shutdown();
        cache.values().forEach(Asset::dispose);
        cache.clear();
    }
}