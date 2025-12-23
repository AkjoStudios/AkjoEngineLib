package com.akjostudios.engine.runtime.impl.render.renderers;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import com.akjostudios.engine.res.ShaderResources;
import com.akjostudios.engine.runtime.exceptions.RenderResourceNotReadyException;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

public final class SpriteRenderer extends AbstractCanvasRenderer {
    private Shader shader;
    private int vao, vbo, ebo;

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f model = new Matrix4f();

    private static final float[] VERTICES = {
            0f, 1f, 0f, 1f, // BL
            1f, 0f, 1f, 0f, // TR
            0f, 0f, 0f, 0f, // TL
            1f, 1f, 1f, 1f  // BR
    };

    private static final int[] INDICES = { 2, 1, 0, 0, 1, 3 };

    @Override
    protected void init(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception {
        this.shader = device.assets().get(ShaderResources.SPRITE);
        if (shader == null) {
            throw new RenderResourceNotReadyException("ℹ️  Sprite shader is not ready yet!");
        }

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VERTICES, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, INDICES, GL15.GL_STATIC_DRAW);

        int stride = 4 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected void onBegin(@NotNull RenderDevice device, @NotNull FrameInfo info) {
        projection.identity().ortho(
                0F, (float) info.resolution().width(),
                (float) info.resolution().height(), 0F,
                -1F, 1F
        );

        shader.bind();
        shader.setUniform("uProjection", projection);
        shader.setUniform("uTexture", 0);
    }

    public void draw(@NotNull Texture texture, @NotNull IRenderPosition position) {
        float x = position.x();
        float y = position.y();
        float width = texture.resolution().width();
        float height = texture.resolution().height();

        model.identity().translate(x, y, 0F).scale(width, height, 1F);
        shader.setUniform("uModel", model);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        texture.bind();

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0L);
        GL30.glBindVertexArray(0);
    }

    @Override
    protected void onEnd() {
        shader.unbind();
    }

    @Override
    protected void onDispose() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ebo);
    }
}