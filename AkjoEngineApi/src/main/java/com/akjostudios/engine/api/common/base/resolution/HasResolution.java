package com.akjostudios.engine.api.common.base.resolution;

/**
 * Applies to objects that have a resolution in pixels.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasResolution {
    IResolution resolution();
}