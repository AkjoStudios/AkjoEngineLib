package com.akjostudios.engine.api;

import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class AkjoApplication implements IAkjoApplication {
    protected Logger log;
    protected IAkjoApplicationContext ctx;

    @Override
    public void onInit() {}

    @Override
    public void onStart() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onPause() {}

    @Override
    public void onResume() {}

    @Override
    public void onStop() {}

    @Override
    public void onDestroy() {}

    /**
     * Sets the context for this application.
     * @apiNote Should only be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    public final void __engine_setContext(
            @NotNull Object token,
            @NotNull IAkjoApplicationContext ctx
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        this.ctx = ctx;
    }

    /**
     * Sets the internal logger for this application.
     * @apiNote Should only be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    public final void __engine_setLogger(
            @NotNull Object token,
            @NotNull Logger logger
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        this.log = logger;
    }
}