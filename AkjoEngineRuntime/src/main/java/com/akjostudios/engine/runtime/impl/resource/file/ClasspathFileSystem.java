package com.akjostudios.engine.runtime.impl.resource.file;

import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.time.Instant;
import java.util.List;

@SuppressWarnings({"unused"})
public final class ClasspathFileSystem implements FileSystem {
    private final ClassLoader loader;
    private final String root;

    public ClasspathFileSystem(@NotNull ClassLoader loader, @NotNull String root) {
        this.loader = loader;
        this.root = root.isEmpty() ? "" : (root.endsWith("/") ? root : root + "/");
    }

    @Override
    public @NotNull SeekableByteChannel open(@NotNull ResourcePath path) throws IOException {
        URL url = getPath(path);
        if (url == null) { throw new FileNotFoundException(path.toString()); }

        try (InputStream stream = url.openStream()) {
            byte[] bytes = stream.readAllBytes();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            return new ReadOnlyMemoryChannel(buffer);
        }
    }

    @Override
    public @NotNull InputStream openStream(@NotNull ResourcePath path) throws IOException {
        ensureScheme(path.scheme());
        String fullPath = full(path.path());
        InputStream stream = loader.getResourceAsStream(fullPath);
        if (stream == null) { throw new FileNotFoundException(fullPath); }
        return stream;
    }

    @Override
    public boolean exists(@NotNull ResourcePath path) {
        return getPath(path) != null;
    }

    @Override
    public long size(@NotNull ResourcePath path) {
        URL url = getPath(path);
        if (url == null) { return -1L; }

        try {
            URLConnection conn = url.openConnection();
            long len = conn.getContentLengthLong();
            if (len == -1) {
                try (InputStream is = conn.getInputStream()) {
                    return is.readAllBytes().length;
                }
            }
            return len;
        } catch (IOException e) {
            return -1L;
        }
    }

    /**
     * @apiNote Getting the lastModified instant from the classpath is slow and sometimes unreliable. If access to lastModified is needed, try other resource loading methods first.
     */
    @Override
    public @NotNull Instant lastModified(@NotNull ResourcePath path) {
        URL url = getPath(path);
        if (url == null) { return Instant.EPOCH; }

        try {
            return Instant.ofEpochMilli(url.openConnection().getLastModified());
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    /**
     * @apiNote Classpath listing is unreliable and not supported.
     * @return Always an empty unmodifiable list
     */
    @Override
    public @NotNull List<ResourcePath> list(ResourcePath path) {
        return List.of();
    }

    private @Nullable URL getPath(@NotNull ResourcePath path) {
        ensureScheme(path.scheme());
        return loader.getResource(full(path.path()));
    }

    private @NotNull String full(@NotNull String path) {
        return root + (path.startsWith("/") ? path.substring(1) : path);
    }

    private static void ensureScheme(ResourcePath.Scheme scheme) {
        if (scheme != ResourcePath.Scheme.CLASSPATH) {
            throw new IllegalArgumentException("❗ The classpath file system only supports classpath paths!");
        }
    }

    private static final class ReadOnlyMemoryChannel implements SeekableByteChannel {
        private final ByteBuffer buffer;
        private boolean open = true;

        private ReadOnlyMemoryChannel(@NotNull ByteBuffer buffer) {
            this.buffer = buffer.asReadOnlyBuffer();
        }

        @Override
        public int read(@NotNull ByteBuffer dst) throws IOException {
            if (!open) { throw new ClosedChannelException(); }
            int n = Math.min(dst.remaining(), buffer.remaining());
            if (n <= 0) { return -1; }
            int limit = buffer.limit();
            buffer.limit(buffer.position() + n);
            dst.put(buffer);
            buffer.limit(limit);
            return n;
        }

        @Override
        public int write(@NotNull ByteBuffer src) { throw new NonWritableChannelException(); }

        @Override
        public long position() { return buffer.position(); }

        @Override
        public SeekableByteChannel position(long newPosition) {
            buffer.position((int) newPosition);
            return this;
        }

        @Override
        public long size() { return buffer.limit(); }

        @Override
        public SeekableByteChannel truncate(long size) { throw new NonWritableChannelException(); }

        @Override
        public boolean isOpen() { return open; }

        @Override
        public void close() { open = false; }
    }
}