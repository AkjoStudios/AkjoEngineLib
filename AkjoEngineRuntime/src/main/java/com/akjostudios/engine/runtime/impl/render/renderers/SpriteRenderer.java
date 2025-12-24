package com.akjostudios.engine.runtime.impl.render.renderers;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.render.IRenderPosition;
import com.akjostudios.engine.api.render.context.FrameInfo;
import com.akjostudios.engine.api.render.context.RenderDevice;
import com.akjostudios.engine.res.ShaderResources;
import com.akjostudios.engine.runtime.exceptions.RenderResourceNotReadyException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public final class SpriteRenderer extends AbstractCanvasRenderer {
    private static final int MAX_SPRITES = 2000;

    private static final int VERTEX_FLOATS = 4;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int INDICES_PER_SPRITE = 6;

    private static final String PROJECTION_UNIFORM = "uProjection";
    private static final String TEXTURE_UNIFORM = "uTexture";

    private Shader shader;
    private int vao, vbo, ebo;

    private final Matrix4f projection = new Matrix4f();

    private FloatBuffer vertexBuffer;
    private int spriteCount = 0;

    private Texture currentTexture;

    @Override
    protected void init(@NotNull RenderDevice device, @NotNull FrameInfo info) throws Exception {
        this.shader = device.assets().get(ShaderResources.SPRITE);
        if (shader == null) {
            throw new RenderResourceNotReadyException(
                    "ℹ️  Sprite shader is not ready yet!"
            );
        }

        int maxVertices = MAX_SPRITES * VERTICES_PER_SPRITE * VERTEX_FLOATS;
        vertexBuffer = MemoryUtil.memAllocFloat(maxVertices);

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER,
                (long) maxVertices * Float.BYTES,
                GL15.GL_DYNAMIC_DRAW
        );

        int[] indices = buildIndices();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(
                GL15.GL_ELEMENT_ARRAY_BUFFER,
                indices,
                GL15.GL_STATIC_DRAW
        );

        int stride = VERTEX_FLOATS * Float.BYTES;

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
        spriteCount = 0;
        currentTexture = null;
        vertexBuffer.clear();

        projection.identity().ortho(
                0F, (float) info.resolution().width(),
                (float) info.resolution().height(), 0F,
                -1F, 1F
        );

        shader.bind();
        shader.setUniform(PROJECTION_UNIFORM, projection);
        shader.setUniform(TEXTURE_UNIFORM, 0);
    }

    public void draw(@NotNull Texture texture, @NotNull IRenderPosition position) {
        if (currentTexture != null && currentTexture != texture) {
            flush();
        }
        currentTexture = texture;

        if (spriteCount >= MAX_SPRITES) {
            flush();
        }

        float x1 = position.x();
        float y1 = position.y();

        float width = texture.resolution().width();
        float height = texture.resolution().height();

        float x2 = x1 + width;
        float y2 = y1 + height;

        putVertex(x1, y2, 0f, 1f); // BL
        putVertex(x2, y1, 1f, 0f); // TR
        putVertex(x1, y1, 0f, 0f); // TL
        putVertex(x2, y2, 1f, 1f); // BR

        spriteCount++;
    }

    @Override
    protected void onEnd() {
        flush();
        shader.unbind();
    }

    @Override
    protected void onDispose() {
        flush();

        if (vertexBuffer != null) {
            MemoryUtil.memFree(vertexBuffer);
            vertexBuffer = null;
        }

        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ebo);
    }

    private void flush() {
        if (spriteCount == 0) {
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        currentTexture.bind();

        vertexBuffer.flip();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);

        int indexCount = spriteCount * INDICES_PER_SPRITE;
        GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0L);

        GL30.glBindVertexArray(0);

        vertexBuffer.clear();
        spriteCount = 0;
    }

    private void putVertex(float x, float y, float u, float v) {
        vertexBuffer.put(x).put(y).put(u).put(v);
    }

    @Contract(pure = true)
    private static int@NotNull[] buildIndices() {
        int[] indices = new int[MAX_SPRITES * INDICES_PER_SPRITE];
        int offset = 0;
        for (int i = 0; i < MAX_SPRITES; i++) {
            int base = i * INDICES_PER_SPRITE;
            indices[base] = offset + 2;
            indices[base + 1] = offset + 1;
            indices[base + 2] = offset;
            indices[base + 3] = offset;
            indices[base + 4] = offset + 1;
            indices[base + 5] = offset + 3;
            offset += 4;
        }
        return indices;
    }
}