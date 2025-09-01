package com.akjostudios.engine.api.internal.token;

public final class EngineTokens {
    private static final EngineToken TOKEN = new EngineToken();

    public static Object token() { return TOKEN; }

    public static void verify(Object candidate) throws IllegalCallerException {
        if (candidate != TOKEN) {
            throw new IllegalCallerException("❗ '__engine' methods require a valid runtime token as an argument! Please do not call these methods directly.");
        }
    }
}