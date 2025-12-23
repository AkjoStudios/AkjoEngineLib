package com.akjostudios.engine.runtime.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGLUtil {
    public static boolean contextFail() {
        if (GLFW.glfwGetCurrentContext() == 0) { return true; }
        try {
            GL.getCapabilities();
        } catch (IllegalStateException e) {
            return true;
        }
        return false;
    }

    public static void contextFail(
            @NotNull String action
    ) {
        if (GLFW.glfwGetCurrentContext() == 0) {
            throw new IllegalStateException("❌  Failed to " + action + " as there is no active OpenGL context!");
        }
        try {
            GL.getCapabilities();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("❌  Failed to " + action + " without any OpenGL capabilities!");
        }
    }
}