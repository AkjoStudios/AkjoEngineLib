package com.akjostudios.engine.runtime.impl.assets.texture;

import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.assets.texture.TextureResolution;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.runtime.util.OpenGLUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Accessors(fluent = true)
public final class TextureImpl implements Texture {
    @Getter private final ResourcePath path;
    @Getter private final int id;
    @Getter private final TextureResolution resolution;

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void bind() {
        if (disposed.get()) {
            throw new IllegalStateException("❗ Attempted to bind a disposed texture at \"" + path + "\"!");
        }
        if (OpenGLUtil.contextFail()) { return; }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            if (GLFW.glfwGetCurrentContext() != 0L) {
                GL11.glDeleteTextures(id);
            }
        }
    }

    @Override
    public String toString() {
        return "TextureImpl[" + path + "]" + "(" +
                "id=" + id() + ", " +
                "width=" + resolution.width() + ", " +
                "height=" + resolution.height() + ")";
    }
}