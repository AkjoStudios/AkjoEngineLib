package com.akjostudios.engine.runtime.impl.assets.shader;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.assets.texture.Texture;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.runtime.util.OpenGLUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class ShaderImpl implements Shader {
    @Getter private final ResourcePath path;
    private final int programId;

    private final Map<String, Integer> locationCache = new HashMap<>();
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void bind() {
        ensureNotDisposed();
        if (OpenGLUtil.contextFail()) { return; }

        GL20.glUseProgram(programId);
    }

    @Override
    public void unbind() {
        if (OpenGLUtil.contextFail()) { return; }

        GL20.glUseProgram(0);
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            if (GLFW.glfwGetCurrentContext() != 0L) {
                GL20.glDeleteProgram(programId);
            }
        }
    }

    @Override
    public void setUniform(@NotNull String name, int value) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1i(location, value);
        }
    }

    @Override
    public void setUniform(@NotNull String name, float value) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1f(location, value);
        }
    }

    @Override
    public void setUniform(@NotNull String name, boolean value) {
        setUniform(name, value ? 1 : 0);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector2fc value) {
        setUniform(name, value.x(), value.y());
    }

    @Override
    public void setUniform(@NotNull String name, float x, float y) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform2f(location, x, y);
        }
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector3fc value) {
        setUniform(name, value.x(), value.y(), value.z());
    }

    @Override
    public void setUniform(@NotNull String name, float x, float y, float z) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform3f(location, x, y, z);
        }
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector4fc value) {
        setUniform(name, value.x(), value.y(), value.z(), value.w());
    }

    @Override
    public void setUniform(@NotNull String name, float x, float y, float z, float w) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform4f(location, x, y, z, w);
        }
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Matrix3fc value) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            value.get(buffer);
            GL20.glUniformMatrix3fv(location, false, buffer);
        }
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Matrix4fc value) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            GL20.glUniformMatrix4fv(location, false, buffer);
        }
    }

    @Override
    public void setUniform(@NotNull String name, int@NotNull[] values) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1iv(location, values);
        }
    }

    @Override
    public void setUniform(@NotNull String name, float@NotNull[] values) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1fv(location, values);
        }
    }

    @Override
    public void bindTexture(@NotNull String name, @NotNull Texture texture, int slot) {
        if (OpenGLUtil.contextFail()) { return; }

        int location = getUniformLocation(name);
        if (location == -1) {
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        texture.bind();
        GL20.glUniform1i(location, slot);
    }

    @Override
    public boolean hasUniform(@NotNull String name) {
        return getUniformLocation(name) != -1;
    }

    private int getUniformLocation(@NotNull String name) {
        ensureNotDisposed();
        if (locationCache.containsKey(name)) {
            return locationCache.get(name);
        }

        int location = GL20.glGetUniformLocation(programId, name);
        locationCache.put(name, location);

        return location;
    }

    private void ensureNotDisposed() {
        if (disposed.get()) {
            throw new IllegalStateException("❗ Attempted to use disposed shader at \"" + path + "\"!");
        }
    }
}