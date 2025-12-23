package com.akjostudios.engine.api.common.base.size;

/**
 * Applies to objects that have a size in physical 2D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasSize2D {
    ISize2D size();
}