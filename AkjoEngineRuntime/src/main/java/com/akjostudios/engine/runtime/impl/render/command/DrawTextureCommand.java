package com.akjostudios.engine.runtime.impl.render.command;

import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.render.command.RenderCommand;
import org.jetbrains.annotations.NotNull;

public record DrawTextureCommand(
        @NotNull Texture texture,
        @NotNull IRenderPosition position
) implements RenderCommand {}