package com.akjostudios.engine.api.common.base.scale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class Scales {
    /**
     * @return The equivalent isotropic scale for the given scale in 2D space.
     */
    public static double equivalentIsotropic(@NotNull IScale2D scale) {
        return Math.sqrt(scale.scaleX() * scale.scaleY());
    }

    /**
     * @return The equivalent anisotropic scale for the given scale in 3D space.
     */
    public static double equivalentAnisotropic(@NotNull IScale3D scale) {
        return Math.sqrt(scale.scaleX() * scale.scaleY() * scale.scaleZ());
    }
}
