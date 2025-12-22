package com.akjostudios.engine.runtime.impl.assets.shader;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.resource.asset.AssetLoader;
import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.runtime.util.OpenGLUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ShaderLoader implements AssetLoader<Shader, ShaderLoader.Source> {
    public record Source(
            @NotNull String vertexSource,
            @NotNull String fragmentSource
    ) {}

    private static final String VERT_EXTENSION = ".vert";
    private static final String FRAG_EXTENSION = ".frag";

    @Override
    public @NotNull Source loadRaw(@NotNull ResourcePath path, @NotNull FileSystem fs) throws Exception {
        ResourcePath vertPath = appendExtension(path, VERT_EXTENSION);
        ResourcePath fragPath = appendExtension(path, FRAG_EXTENSION);

        String vertSrc = readFile(fs, vertPath);
        String fragSrc = readFile(fs, fragPath);

        return new Source(vertSrc, fragSrc);
    }

    @Override
    public @NotNull Shader createAsset(@NotNull ResourcePath path, @NotNull ShaderLoader.Source data) {
        OpenGLUtil.checkContext("create shader at \"" + path + "\"");

        int programId = GL20.glCreateProgram();
        int vertId = compileShader(GL20.GL_VERTEX_SHADER, data.vertexSource(), path + VERT_EXTENSION);
        int fragId = compileShader(GL20.GL_FRAGMENT_SHADER, data.fragmentSource(), path + FRAG_EXTENSION);

        GL20.glAttachShader(programId, vertId);
        GL20.glAttachShader(programId, fragId);

        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programId);
            GL20.glDeleteShader(vertId);
            GL20.glDeleteShader(fragId);
            GL20.glDeleteProgram(programId);

            throw new RuntimeException("❗ Error linking Shader[" + path + "]:\n" + log);
        }

        GL20.glDetachShader(programId, vertId);
        GL20.glDetachShader(programId, fragId);
        GL20.glDeleteShader(vertId);
        GL20.glDeleteShader(fragId);

        GL20.glValidateProgram(programId);

        return new ShaderImpl(path, programId);
    }

    private int compileShader(int type, @NotNull String source, @NotNull String name) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);

        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(id);
            GL20.glDeleteShader(id);
            throw new RuntimeException(
                    "❗ Error compiling " +
                    (type == GL20.GL_VERTEX_SHADER ? "Vertex" : "Fragment") +
                    " shader [" + name + "]:\n" + log
            );
        }

        return id;
    }

    private @NotNull String readFile(
            @NotNull FileSystem fs,
            @NotNull ResourcePath path
    ) throws IOException {
        try (InputStream stream = fs.openStream(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private @NotNull ResourcePath appendExtension(
            @NotNull ResourcePath original,
            @NotNull String extension
    ) {
        return new ResourcePath(original.scheme(), original.path() + extension);
    }
}