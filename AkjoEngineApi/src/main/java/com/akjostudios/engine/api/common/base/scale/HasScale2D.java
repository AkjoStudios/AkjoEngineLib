package com.akjostudios.engine.api.common.base.scale;

/**
 * Applies to objects that have a scale in 2D space.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasScale2D {
    IScale2D scale();
}