package com.akjostudios.engine.api.common.base.scale;

/**
 * Applies to objects that have a scale in 3D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasScale3D {
    IScale3D scale();
}