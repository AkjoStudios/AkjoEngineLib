package com.akjostudios.engine.api.resource.file;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MountableFileSystem extends FileSystem {
    /**
     * Mounts the given file system as the given alias under the given base path.
     */
    void mount(@NotNull String alias, @NotNull FileSystem fs, @NotNull String basePath);
}