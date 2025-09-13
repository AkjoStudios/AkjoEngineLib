package com.akjostudios.engine.api.common.base;

/**
 * Applies to objects that have a visibility.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasVisibility {
    boolean visible();
}