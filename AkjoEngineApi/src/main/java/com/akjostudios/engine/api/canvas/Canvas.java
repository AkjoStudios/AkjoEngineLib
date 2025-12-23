package com.akjostudios.engine.api.canvas;

import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.common.base.color.IColor;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.window.WindowPositionProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Canvas {
    /**
     * Clears the canvas with the given color.
     */
    void clear(@NotNull IColor color);

    /**
     * Draws the given texture to the given position on the canvas.
     */
    void drawTexture(
            @NotNull Texture texture,
            @NotNull IRenderPosition position
    );

    /**
     * Draws the given texture to the given position on the canvas.
     */
    void drawTexture(
            @NotNull Texture texture,
            @NotNull WindowPositionProvider position
    );
}