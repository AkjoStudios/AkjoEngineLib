package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.common.Disposable;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Asset extends Disposable {
    /**
     * @return The resource path this asset was loaded from.
     */
    @NotNull ResourcePath path();

    /**
     * Releases GPU resources (textures, buffers, etc.) associated with this asset.
     * This is usually called automatically by the AssetManager when unloading or when the application stops.
     */
    @Override
    void dispose();
}