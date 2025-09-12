package com.akjostudios.engine.api.common.base;

/**
 * Applies to objects that have a name.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasName {
    String name();
}