package com.akjostudios.engine.runtime.impl.canvas;

import com.akjostudios.engine.api.canvas.Canvas;
import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.runtime.impl.render.commands.ClearCommand;
import com.akjostudios.engine.runtime.impl.render.commands.RenderCommand;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public final class CanvasImpl implements Canvas {
    private final ConcurrentLinkedQueue<RenderCommand> queue = new ConcurrentLinkedQueue<>();
    private final Runnable onRenderRequest;

    @Override
    public void clear(@NotNull IColor color) {
        queue.add(new ClearCommand(color));
        onRenderRequest.run();
    }

    public void drainTo(@NotNull RenderCommand.Sink sink) {
        RenderCommand command;
        while ((command = queue.poll()) != null) {
            sink.accept(command);
        }
    }
}