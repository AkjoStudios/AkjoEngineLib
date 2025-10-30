package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetLoader<I> {
    boolean supports(@NotNull ResourcePath path, Map<String, Object> params);
    @NotNull Optional<I> load(
            @NotNull FileSystem fs,
            @NotNull ResourcePath path,
            @NotNull Map<String, Object> params
    ) throws IOException;
}