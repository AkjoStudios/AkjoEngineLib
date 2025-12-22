package com.akjostudios.engine.api.assets;

import com.akjostudios.engine.api.resource.asset.Asset;

@SuppressWarnings("unused")
public interface Texture extends Asset {
    /**
     * @return The OpenGL ID of this texture.
     */
    int id();

    /**
     * @return The width of the texture in pixels.
     */
    int width();

    /**
     * @return The height of the texture in pixels.
     */
    int height();

    /**
     * Binds this texture to the currently active texture unit (GL_TEXTURE_2D)
     */
    void bind();
}