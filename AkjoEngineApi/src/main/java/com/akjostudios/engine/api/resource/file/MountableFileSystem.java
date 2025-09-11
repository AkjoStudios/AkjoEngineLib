package com.akjostudios.engine.api.resource.file;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MountableFileSystem extends FileSystem {
    /**
     * Mounts the given file system as the given alias under the given base path.
     * @apiNote This method is generally not manually called, as no file system implementations are exported to the application module. Use the pre-defined aliases (assets, etc.) instead.
     */
    void mount(@NotNull String alias, @NotNull FileSystem fs, @NotNull String basePath);
}