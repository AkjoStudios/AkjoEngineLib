package com.akjostudios.engine.runtime.impl.canvas;

import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.canvas.Canvas;
import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.render.command.RenderCommand;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowPositionProvider;
import com.akjostudios.engine.runtime.impl.render.command.ClearCommand;
import com.akjostudios.engine.runtime.impl.render.command.DrawTextureCommand;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public final class CanvasImpl implements Canvas {
    private final AtomicReference<ConcurrentLinkedQueue<RenderCommand>> queueRef =
            new AtomicReference<>(new ConcurrentLinkedQueue<>());

    private final Window window;

    @Override
    public void clear(@NotNull IColor color) {
        queueRef.get().add(new ClearCommand(color));
        window.requestRender();
    }

    @Override
    public void drawTexture(
            @NotNull Texture texture,
            @NotNull IRenderPosition position
    ) {
        queueRef.get().add(new DrawTextureCommand(texture, position));
        window.requestRender();
    }

    @Override
    public void drawTexture(
            @NotNull Texture texture,
            @NotNull WindowPositionProvider position
    ) {
        drawTexture(texture, position.retrieve(window));
    }

    public @NotNull ConcurrentLinkedQueue<RenderCommand> swapQueue() {
        return queueRef.getAndSet(new ConcurrentLinkedQueue<>());
    }
}