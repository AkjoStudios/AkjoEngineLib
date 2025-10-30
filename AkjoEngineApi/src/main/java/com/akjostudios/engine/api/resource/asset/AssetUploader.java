package com.akjostudios.engine.api.resource.asset;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AssetUploader<I, O> {
    boolean supports(
            @NotNull Class<I> input,
            @NotNull Class<O> output,
            @NotNull Map<String, Object> params
    );

    @NotNull Optional<O> upload(
            @NotNull I cpu,
            @NotNull Map<String, Object> params
    ) throws Exception;
}