package com.akjostudios.engine.api.common.base.size;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class Sizes {
    /**
     * @return The area of the given size in millimeters.
     */
    public static double areaMm(@NotNull ISize2D size) {
        return size.widthMm() * size.heightMm();
    }

    /**
     * @return The volume of the given size in millimeters.
     */
    public static double volumeMm(@NotNull ISize3D size) {
        return size.widthMm() * size.heightMm() * size.depthMm();
    }
}
