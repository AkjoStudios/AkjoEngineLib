package com.akjostudios.engine.api.resource.asset;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetDecoder<I, O> {
    boolean supports(@NotNull Class<I> in, @NotNull Class<O> out, @NotNull Map<String, Object> params);
    @NotNull Optional<O> decode(@NotNull I input, @NotNull Map<String, Object> params) throws Exception;
}