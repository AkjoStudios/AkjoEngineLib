package com.akjostudios.engine.runtime.impl.render.backend;

import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.runtime.impl.render.commands.ClearCommand;
import com.akjostudios.engine.runtime.impl.render.commands.RenderCommand;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public final class CanvasRenderBackend implements RenderBackend {
    private IColor lastClearColor;

    @Override
    public void execute(@NotNull RenderCommand command) {
        switch (command) {
            case ClearCommand(IColor color) -> clear(color);
        }
    }

    private void clear(@NotNull IColor color) {
        GL11.glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        this.lastClearColor = color;
    }

    @Override
    public void flush() {
        if (this.lastClearColor != null) {
            clear(this.lastClearColor);
            this.lastClearColor = null;
        }
    }
}