package com.akjostudios.engine.api.common.base.color;

/**
 * Applies to objects that have a color.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasColor {
    IColor color();
}