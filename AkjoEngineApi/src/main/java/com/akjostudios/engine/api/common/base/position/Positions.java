package com.akjostudios.engine.api.common.base.position;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class Positions {
    /**
     * @return The distance between the given positions in 2D space.
     */
    public static double distance(@NotNull IPosition2D pos1, @NotNull IPosition2D pos2) {
        return Math.sqrt(Math.pow(pos1.x() - pos2.x(), 2) + Math.pow(pos1.y() - pos2.y(), 2));
    }

    /**
     * @return The distance between the given positions in 3D space.
     */
    public static double distance(@NotNull IPosition3D pos1, @NotNull IPosition3D pos2) {
        return Math.sqrt(Math.pow(pos1.x() - pos2.x(), 2) + Math.pow(pos1.y() - pos2.y(), 2) + Math.pow(pos1.z() - pos2.z(), 2));
    }
}