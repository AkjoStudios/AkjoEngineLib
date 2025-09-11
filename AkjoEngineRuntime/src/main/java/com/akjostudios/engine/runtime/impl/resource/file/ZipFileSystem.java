package com.akjostudios.engine.runtime.impl.resource.file;

import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unused")
public final class ZipFileSystem extends PathBasedFileSystem implements AutoCloseable {
    private final java.nio.file.FileSystem delegate;
    private final Path root;

    public ZipFileSystem(@NotNull Path zipFile, @NotNull String root) throws IOException {
        this.delegate = FileSystems.newFileSystem(
                URI.create("jar:" + zipFile.toUri()),
                Map.of("create", "false")
        );
        this.root = root.isEmpty() ? delegate.getPath("/") : delegate.getPath(root);
    }

    @Override
    public void close() throws Exception { delegate.close(); }

    @Override
    protected @NotNull Path toPath(@NotNull ResourcePath path) throws IllegalArgumentException {
        if (path.scheme() != ResourcePath.Scheme.ZIP) {
            throw new IllegalArgumentException("❗ The zip file system only supports zip paths!");
        }
        String relativePath = path.path().startsWith("/") ? path.path().substring(1) : path.path();
        return root.resolve(relativePath);
    }
}