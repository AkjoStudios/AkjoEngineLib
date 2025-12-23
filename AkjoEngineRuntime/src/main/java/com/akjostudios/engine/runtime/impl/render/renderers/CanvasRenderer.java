package com.akjostudios.engine.runtime.impl.render.renderers;

import com.akjostudios.engine.api.common.Disposable;
import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import org.jetbrains.annotations.NotNull;

public interface CanvasRenderer extends Disposable {
    void begin(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception;
    void end();
}