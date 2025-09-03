package com.akjostudios.engine.runtime.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathUtil {
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}