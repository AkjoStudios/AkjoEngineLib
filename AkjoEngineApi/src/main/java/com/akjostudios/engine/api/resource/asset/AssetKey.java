package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record AssetKey<T>(
        @NotNull Class<T> type,
        @NotNull ResourcePath path,
        @NotNull Map<String, Object> params
) {
    @Override
    public @NotNull String toString() {
        return "AssetKey(name=" + type.getSimpleName() + ", path=" + path + ", params=" + params + ")";
    }
}