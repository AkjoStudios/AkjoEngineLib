package com.akjostudios.engine.runtime.impl;

import com.akjostudios.engine.api.AkjoApplication;

public class DefaultAkjoApplication extends AkjoApplication {
    @Override
    public void onUpdate(double deltaTime) {
        if (ctx.lifecycle().isRunning()) {
            log.info("🔠 This is the default application. No code will be executed and the engine will shut down.");
        }
        if (!ctx.lifecycle().isStopping()) {
            ctx.threading().runOnWorker(() -> ctx.lifecycle().stopApplication("Default Application"));
        }
    }
}