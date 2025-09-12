package com.akjostudios.engine.api.common.base.size;

/**
 * Applies to objects that have a size in 3D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasSize3D {
    ISize3D size();
}