package com.akjostudios.engine.runtime.impl.resource.asset;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.assets.Text;
import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.common.Disposable;
import com.akjostudios.engine.api.resource.asset.Asset;
import com.akjostudios.engine.api.resource.asset.AssetHandle;
import com.akjostudios.engine.api.resource.asset.AssetLoader;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.scheduling.SchedulerLane;
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
    private record Key(@NotNull ResourcePath path, @NotNull Class<?> type) {}

    private final FileSystem fs;
    private final Scheduler scheduler;

    private final ExecutorService executor;

    private final Map<Key, Asset> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, AssetLoader<?, ?>> loaders = new HashMap<>();
    private final Map<Class<?>, SchedulerLane> laneMap = new HashMap<>();

    private final Map<Key, CompletableFuture<? extends Asset>> inFlight = new ConcurrentHashMap<>();

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

    public @NotNull AssetManagerImpl setup() {
        register(Text.class, new TextLoader(), SchedulerLane.LOGIC);
        register(Texture.class, new TextureLoader(), SchedulerLane.RENDER);
        register(Shader.class, new ShaderLoader(), SchedulerLane.RENDER);

        return this;
    }

    private <T extends Asset, D> void register(@NotNull Class<T> type, @NotNull AssetLoader<T, D> loader, @NotNull SchedulerLane lane) {
        loaders.put(type, loader);
        laneMap.put(type, lane);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Asset> @NotNull CompletableFuture<T> loadAsync(@NotNull ResourcePath path, @NotNull Class<T> type) {
        Key key = new Key(path, type);

        Asset cached = cache.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture((T) cached);
        }

        return (CompletableFuture<T>) inFlight.computeIfAbsent(key, _ -> {
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

                    scheduler.runImmediately(() -> {
                        try {
                            T asset = loader.createAsset(path, intermediate);
                            cache.put(key, asset);
                            future.complete(asset);
                        } catch (Throwable t) {
                            future.completeExceptionally(t);
                        } finally {
                            inFlight.remove(key);
                        }
                    }, laneMap.getOrDefault(type, SchedulerLane.RENDER));
                } catch (Throwable t) {
                    try {
                        future.completeExceptionally(t);
                    } finally {
                        inFlight.remove(key);
                    }
                }
            });

            return future;
        });
    }

    @Override
    public <T extends Asset> @NotNull CompletableFuture<T> loadAsync(@NotNull EngineResources<T> resource) {
        return loadAsync(resource.path(), resource.type());
    }

    @Override
    public <T extends Asset> @NotNull T load(@NotNull ResourcePath path, @NotNull Class<T> type) {
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
    public <T extends Asset> @NotNull T load(@NotNull EngineResources<T> resource) {
        return load(resource.path(), resource.type());
    }

    @Override
    public <T extends Asset> @NotNull AssetHandle<T> handle(@NotNull ResourcePath path, @NotNull Class<T> type) {
        return new AssetHandleImpl<>(this, path, type);
    }

    @Override
    public <T extends Asset> @NotNull AssetHandle<T> preloadHandle(@NotNull ResourcePath path, @NotNull Class<T> type) {
        AssetHandle<T> handle = handle(path, type);
        handle.preload();
        return handle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Asset> @Nullable T get(@NotNull ResourcePath path, @NotNull Class<T> type) {
        return (T) cache.get(new Key(path, type));
    }

    @Override
    public <T extends Asset> void unload(@NotNull ResourcePath path, @NotNull Class<T> type) {
        Asset asset = cache.remove(new Key(path, type));
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