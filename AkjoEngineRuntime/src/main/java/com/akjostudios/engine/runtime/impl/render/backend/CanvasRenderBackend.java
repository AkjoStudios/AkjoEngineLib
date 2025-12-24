package com.akjostudios.engine.runtime.impl.render.backend;

import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.render.backend.RenderBackend;
import com.akjostudios.engine.api.render.command.RenderCommand;
import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import com.akjostudios.engine.runtime.exceptions.RenderResourceNotReadyException;
import com.akjostudios.engine.runtime.impl.render.command.ClearCommand;
import com.akjostudios.engine.runtime.impl.render.command.DrawTextureCommand;
import com.akjostudios.engine.runtime.impl.render.renderers.SpriteRenderer;
import com.akjostudios.engine.runtime.util.OpenGLUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public final class CanvasRenderBackend implements RenderBackend {
    private RenderDevice device;
    private FrameInfo currentFrame;

    private IColor lastClearColor;

    private final SpriteRenderer sprites = new SpriteRenderer();
    private boolean spritesBegun = false;

    @Override
    public void init(@NotNull RenderDevice device) {
        this.device = device;
    }

    @Override
    public void beginFrame(@NotNull FrameInfo frame) {
        this.currentFrame = frame;
    }

    @Override
    public void execute(@NotNull RenderCommand command) throws Exception {
        if (OpenGLUtil.contextFail()) { return; }
        switch (command) {
            case ClearCommand(IColor color) -> lastClearColor = color;
            case DrawTextureCommand(Texture texture, IRenderPosition position) -> {
                flushClearIfNeeded();

                if (!spritesBegun) {
                    try {
                        sprites.begin(device, currentFrame);
                        spritesBegun = true;
                    } catch (RenderResourceNotReadyException e) {
                        device.threading().requestRender();
                    }
                }

                sprites.draw(texture, position);
            }
            default -> {}
        }
    }

    @Override
    public void endFrame() {
        if (spritesBegun) {
            sprites.end();
            spritesBegun = false;
        }
        flushClearIfNeeded();
        this.currentFrame = null;
    }

    @Override
    public void dispose() {
        sprites.dispose();
    }

    private void flushClearIfNeeded() {
        if (lastClearColor == null) {
            return;
        }

        GL11.glClearColor(
                lastClearColor.red(),
                lastClearColor.green(),
                lastClearColor.blue(),
                lastClearColor.alpha()
        );
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        lastClearColor = null;
    }
}