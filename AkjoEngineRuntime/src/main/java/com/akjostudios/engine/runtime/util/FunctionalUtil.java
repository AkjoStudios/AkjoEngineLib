package com.akjostudios.engine.runtime.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionalUtil {
    public static Runnable doNothing() { return () -> {}; }
}