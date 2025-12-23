package com.akjostudios.engine.api.render.backend;

import com.akjostudios.engine.api.common.Disposable;
import com.akjostudios.engine.api.render.command.RenderCommand;
import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import org.jetbrains.annotations.NotNull;

public interface RenderBackend extends Disposable {
    void init(@NotNull RenderDevice device);
    void beginFrame(@NotNull FrameInfo frame);
    void execute(@NotNull RenderCommand command) throws Exception;
    void endFrame();
}