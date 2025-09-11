package com.akjostudios.engine.api.resource.file;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record ResourcePath(
        @NotNull Scheme scheme,
        @NotNull String path
) {
    @Override
    public @NotNull String toString() { return scheme.name().toLowerCase() + "://" + path; }

    /**
     * Creates a new resource path to a local file, ready to be given to the methods of a BasicFileSystem.
     */
    public static @NotNull ResourcePath file(@NotNull String path) {
        return new ResourcePath(Scheme.FILE, path);
    }

    /**
     * Creates a new resource path to a file on the classpath, ready to be given to the methods of a ClasspathFileSystem.
     */
    public static @NotNull ResourcePath classpath(@NotNull String path) {
        return new ResourcePath(Scheme.CLASSPATH, path);
    }

    /**
     * Creates a new resource path to a file inside a zip or jar file, ready to be given to the methods of a ZipFileSystem.
     */
    public static @NotNull ResourcePath zip(@NotNull String path) {
        return new ResourcePath(Scheme.ZIP, path);
    }

    /**
     * Creates a new resource path to a file hosted at an HTTP(s) endpoint, ready to be given to the methods of a HttpFileSystem.
     */
    public static @NotNull ResourcePath http(@NotNull String path) {
        return new ResourcePath(Scheme.HTTP, path);
    }

    public enum Scheme { FILE, CLASSPATH, ZIP, HTTP }
}