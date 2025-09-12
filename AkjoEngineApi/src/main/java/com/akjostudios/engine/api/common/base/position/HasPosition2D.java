package com.akjostudios.engine.api.common.base.position;

/**
 * Applies to objects that have a position in 2D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasPosition2D {
    IPosition2D position();
}