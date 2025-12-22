package com.akjostudios.engine.api.resource.asset;

import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

public interface AssetLoader<T extends Asset, D> {
    /**
     * Reads the necessary data from the virtual file system and decodes that data into RAM.
     * @implNote This executes on a worker thread.
     * @return The intermediate data (for example a ByteBuffer containing raw pixels).
     */
    @NotNull D loadRaw(@NotNull ResourcePath path, @NotNull FileSystem fs) throws Exception;

    /**
     * Takes the intermediate data and creates the usable asset object for the given library (for example OpenGL).
     * @param data The data returned from {@link AssetLoader#loadRaw(ResourcePath, FileSystem)}
     * @implNote This executes on the render thread.
     * @return The final usable asset object.
     */
    @NotNull T createAsset(@NotNull ResourcePath path, @NotNull D data);
}