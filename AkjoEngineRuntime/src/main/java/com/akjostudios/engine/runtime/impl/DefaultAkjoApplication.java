package com.akjostudios.engine.runtime.impl;

import com.akjostudios.engine.api.AkjoApplication;

public class DefaultAkjoApplication extends AkjoApplication {
    @Override
    public void onStart() {
        log.info("🔠 This is the default application. No code will be executed and the engine will shut down.");
        ctx.threading().runOnWorker(() -> ctx.lifecycle().stopApplication("Default Application"));
    }
}