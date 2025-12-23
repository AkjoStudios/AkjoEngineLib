package com.akjostudios.engine.api.assets.texture;

import com.akjostudios.engine.api.resource.asset.Asset;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Texture extends Asset {
    /**
     * @return The OpenGL ID of this texture.
     */
    int id();

    /**
     * @return The resolution of this texture.
     */
    @NotNull TextureResolution resolution();

    /**
     * Binds this texture to the currently active texture unit (GL_TEXTURE_2D)
     */
    void bind();
}