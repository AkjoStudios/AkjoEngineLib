package com.akjostudios.engine.runtime.commands.render;

import com.akjostudios.engine.api.common.base.color.IColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public record ClearCommand(@NotNull IColor color) implements RenderCommand {
    @Override
    public void execute() {
        GL11.glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }
}