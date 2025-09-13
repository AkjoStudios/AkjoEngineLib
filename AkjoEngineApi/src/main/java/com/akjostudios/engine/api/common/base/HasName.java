package com.akjostudios.engine.api.common.base;

import org.jetbrains.annotations.NotNull;

/**
 * Applies to objects that have a name.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasName {
    @NotNull String name();
}