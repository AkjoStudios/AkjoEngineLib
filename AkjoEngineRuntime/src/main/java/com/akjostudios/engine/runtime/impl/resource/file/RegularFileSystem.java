package com.akjostudios.engine.runtime.impl.resource.file;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@SuppressWarnings("unused")
public final class RegularFileSystem extends PathBasedFileSystem {
    protected @NotNull Path toPath(@NotNull ResourcePath path) throws IllegalArgumentException {
        if (path.scheme() != ResourcePath.Scheme.FILE) {
            throw new IllegalArgumentException("❗ The basic file system only supports file paths!");
        }
        return Path.of(path.path());
    }
}