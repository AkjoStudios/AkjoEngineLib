package com.akjostudios.engine.runtime.impl.resource.file;

import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class PathBasedFileSystem implements FileSystem {
    @Override
    public @NotNull SeekableByteChannel open(@NotNull ResourcePath path) throws IOException {
        return Files.newByteChannel(toPath(path), StandardOpenOption.READ);
    }

    @Override
    public @NotNull InputStream openStream(@NotNull ResourcePath path) throws IOException {
        return Files.newInputStream(toPath(path), StandardOpenOption.READ);
    }

    @Override
    public boolean exists(@NotNull ResourcePath path) {
        return Files.exists(toPath(path));
    }

    @Override
    public long size(@NotNull ResourcePath path) {
        try { return Files.size(toPath(path)); }
        catch (IOException e) { return -1L; }
    }

    @Override
    public @NotNull Instant lastModified(@NotNull ResourcePath path) {
        try {
            return Files.getLastModifiedTime(toPath(path)).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    @Override
    public @NotNull List<ResourcePath> list(ResourcePath path) {
        Path dir = toPath(path);
        if (!Files.isDirectory(dir)) { return List.of(); }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<ResourcePath> paths = new ArrayList<>();
            stream.forEach(foundPath -> paths.add(
                    ResourcePath.file(path.toString())
            ));
            return paths;
        } catch (IOException e) {
            return List.of();
        }
    }

    protected abstract @NotNull Path toPath(@NotNull ResourcePath path) throws IllegalArgumentException;
}