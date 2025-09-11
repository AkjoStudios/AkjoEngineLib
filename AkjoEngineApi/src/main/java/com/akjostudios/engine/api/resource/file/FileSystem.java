package com.akjostudios.engine.api.resource.file;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.time.Instant;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface FileSystem {
    /**
     * Opens the given resource path ready for the resource to be read.
     * @return The resource as a byte channel.
     */
    @NotNull SeekableByteChannel open(@NotNull ResourcePath path) throws IOException;

    /**
     * Opens the given resource path ready for the resource to be read.
     * @return The resource as an input stream.
     */
    @NotNull InputStream openStream(@NotNull ResourcePath path) throws IOException;

    /**
     * @return If the given resource path exists.
     */
    boolean exists(@NotNull ResourcePath path);
    /**
     * @return The size of the resource at the given resource path.
     */
    long size(@NotNull ResourcePath path);
    /**
     * @return The point in time where the resource at the given resource path was last modified.
     */
    @NotNull Instant lastModified(@NotNull ResourcePath path);

    /**
     * @apiNote If the path is not a directory, this returns an empty list.
     * @return A list of resource paths inside the given resource path.
     */
    @NotNull List<ResourcePath> list(ResourcePath path);
}