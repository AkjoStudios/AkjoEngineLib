package com.akjostudios.engine.api.assets;

import com.akjostudios.engine.api.resource.asset.Asset;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Text extends Asset {
    /**
     * @return The text contained inside this asset.
     */
    @NotNull String text();
}