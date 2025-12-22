package com.akjostudios.engine.api.assets;

import com.akjostudios.engine.api.resource.asset.Asset;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

@SuppressWarnings("unused")
public interface Shader extends Asset {
    /**
     * Binds this shader program to the OpenGL state.
     * @apiNote All subsequent draw calls will use this shader.
     */
    void bind();

    /**
     * Unbinds the current shader.
     */
    void unbind();

    /**
     * Sets a standard integer uniform.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, int value);

    /**
     * Sets a standard float uniform.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, float value);

    /**
     * Sets a boolean uniform.
     * @apiNote GLSL does not strictly have booleans in uniforms, this sends 0 or 1.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, boolean value);

    /**
     * Sets a 2-component vector uniform (vec2).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, @NotNull Vector2fc value);

    /**
     * Sets a 2-component vector uniform (vec2) using raw floats.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, float x, float y);

    /**
     * Sets a 3-component vector uniform (vec3).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, @NotNull Vector3fc value);

    /**
     * Sets a 3-component vector uniform (vec3) using raw floats.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, float x, float y, float z);

    /**
     * Sets a 4-component vector uniform (vec4).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, @NotNull Vector4fc value);

    /**
     * Sets a 4-component vector uniform (vec4) using raw floats.
     * @apiNote Often used for Colors (R, G, B, A).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, float x, float y, float z, float w);

    /**
     * Sets a 3x3 Matrix uniform (mat3).
     * @apiNote Usually used for normal matrices.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, @NotNull Matrix3fc value);

    /**
     * Sets a 4x4 Matrix uniform (mat4).
     * @apiNote Usually used for model, view, and projection matrices.
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, @NotNull Matrix4fc value);

    /**
     * Sets an array of integers (int[]).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, int@NotNull[] values);

    /**
     * Sets an array of floats (float[]).
     * @param name The name of the uniform variable in the GLSL code.
     */
    void setUniform(@NotNull String name, float@NotNull[] values);

    /**
     * A helper method that binds a texture to a specific slot and tells the shader to use that slot.
     *
     * @param name The name of the 'sampler2D' uniform in the shader.
     * @param texture The texture asset to bind.
     * @param slot The texture unit slot (0-31).
     */
    void bindTexture(@NotNull String name, @NotNull Texture texture, int slot);

    /**
     * Checks if a uniform with the given name exists in the shader source.
     * @apiNote Useful for debugging to check if the compiler optimized out a variable.
     */
    boolean hasUniform(@NotNull String name);
}