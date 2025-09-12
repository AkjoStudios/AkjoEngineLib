package com.akjostudios.engine.api.common.base.area.screen;

/**
 * Applies to objects that have a screen area.
 */
@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HasScreenArea {
    IScreenArea screenArea();
}