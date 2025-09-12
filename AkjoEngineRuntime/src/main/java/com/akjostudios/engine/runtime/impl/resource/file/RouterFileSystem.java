package com.akjostudios.engine.runtime.impl.resource.file;

import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.MountableFileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RouterFileSystem implements MountableFileSystem {
    private final Map<String, Mount> mounts = new ConcurrentHashMap<>();

    @Override
    public void mount(@NotNull String alias, @NotNull FileSystem fs, @NotNull String basePath) {
        mounts.put(alias, new Mount(alias, fs, basePath));
    }

    @Override
    public @NotNull SeekableByteChannel open(@NotNull ResourcePath path) throws IOException {
        Mount mount = resolveMount(path);
        return mount.fileSystem.open(mount.resolve(path));
    }

    @Override
    public @NotNull InputStream openStream(@NotNull ResourcePath path) throws IOException {
        Mount mount = resolveMount(path);
        return mount.fileSystem.openStream(mount.resolve(path));
    }

    @Override
    public boolean exists(@NotNull ResourcePath path) {
        try {
            Mount mount = resolveMount(path);
            return mount.fileSystem.exists(mount.resolve(path));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public long size(@NotNull ResourcePath path) {
        try {
            Mount mount = resolveMount(path);
            return mount.fileSystem.size(mount.resolve(path));
        } catch (IllegalArgumentException e) {
            return -1L;
        }
    }

    @Override
    public @NotNull Instant lastModified(@NotNull ResourcePath path) {
        try {
            Mount mount = resolveMount(path);
            return mount.fileSystem.lastModified(mount.resolve(path));
        } catch (IllegalArgumentException e) {
            return Instant.EPOCH;
        }
    }

    @Override
    public @NotNull List<ResourcePath> list(ResourcePath path) {
        Mount mount = resolveMount(path);
        List<ResourcePath> paths = mount.fileSystem.list(mount.resolve(path));
        String alias = path.path().split("/", 2)[0];
        ResourcePath.Scheme scheme = path.scheme();
        List<ResourcePath> resolved = new ArrayList<>(paths.size());
        paths.forEach(foundPath -> {
            String relativePath = foundPath.path();
            resolved.add(new ResourcePath(scheme, alias + "/" + relativePath));
        });
        return resolved;
    }

    private @NotNull Mount resolveMount(@NotNull ResourcePath path) throws IllegalArgumentException {
        String pathStr = path.path().startsWith("/") ? path.path().substring(1) : path.path();
        int slash = pathStr.indexOf('/');
        String alias = slash == -1 ? pathStr : pathStr.substring(0, slash);
        Mount mount = mounts.get(alias);
        if (mount == null) { throw new IllegalArgumentException("❗ The router file system does not have a mount for alias '" + alias + "'!"); }
        return mount;
    }

    private static @NotNull String normalize(@NotNull String path) {
        if (path.isEmpty()) { return ""; }
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("/")) { normalized = normalized.substring(1); }
        if (normalized.endsWith("/")) { normalized = normalized.substring(0, normalized.length() - 1); }
        return normalized;
    }

    private record Mount(
            @NotNull String alias,
            @NotNull FileSystem fileSystem,
            @NotNull String root
    ) {
            private Mount(@NotNull String alias, @NotNull FileSystem fileSystem, @NotNull String root) {
                this.alias = alias;
                this.fileSystem = fileSystem;
                this.root = normalize(root);
            }

            private @NotNull ResourcePath resolve(@NotNull ResourcePath path) {
                String actualPath = path.path().startsWith("/") ? path.path().substring(1) : path.path();
                String relativePath = actualPath.startsWith(alias + "/") ? actualPath.substring(alias.length() + 1) : actualPath;
                String joined = root.isEmpty() ? relativePath : (root + "/" + relativePath);
                return new ResourcePath(path.scheme(), joined);
            }
        }
}