package com.akjostudios.engine.api.common.base.resolution;

/**
 * Applies to objects that have a screen resolution.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasResolution {
    IResolution resolution();
}