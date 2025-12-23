package com.akjostudios.engine.runtime.impl.render.renderers;

import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCanvasRenderer implements CanvasRenderer {
    private boolean initialized = false;

    @Override
    public final void begin(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception {
        if (!initialized) {
            init(device, info);
            initialized = true;
        }
        onBegin(device, info);
    }

    @Override
    public void end() {
        if (initialized) {
            onEnd();
        }
    }

    @Override
    public void dispose() {
        if (initialized) {
            onDispose();
            initialized = false;
        }
    }

    /**
     * Called exactly once, the first time begin() is called.
     * Use this to load shaders and generate buffers.
     */
    protected abstract void init(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception;

    /**
     * Called every time begin() is called, after initialization checks.
     * Use this to bind shaders, update uniforms, and bind VAOs.
     */
    protected abstract void onBegin(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception;

    /**
     * Called when end() is called.
     * Use this to unbind state (optional).
     */
    protected abstract void onEnd();

    /**
     * Called when the backend is disposed.
     * Use this to delete buffers and arrays.
     */
    protected abstract void onDispose();
}