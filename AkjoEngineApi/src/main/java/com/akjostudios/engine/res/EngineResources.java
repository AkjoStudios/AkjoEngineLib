package com.akjostudios.engine.res;

import com.akjostudios.engine.api.resource.asset.Asset;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

public interface EngineResources<T extends Asset> {
    @NotNull ResourcePath path();
    @NotNull Class<T> type();
}