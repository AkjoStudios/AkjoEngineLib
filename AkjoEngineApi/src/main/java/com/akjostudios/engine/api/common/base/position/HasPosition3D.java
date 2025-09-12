package com.akjostudios.engine.api.common.base.position;

/**
 * Applies to objects that have a position in 3D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasPosition3D {
    IPosition3D position();
}