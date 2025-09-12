package com.akjostudios.engine.api.common.base.resolution;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class Resolutions {
    /**
     * @return The aspect ratio of the given resolution.
     */
    public static double aspectRatio(@NotNull IResolution res) {
        return (double) res.width() / res.height();
    }
}