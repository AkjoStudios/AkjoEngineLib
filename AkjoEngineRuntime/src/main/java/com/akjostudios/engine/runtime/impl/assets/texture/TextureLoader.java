package com.akjostudios.engine.runtime.impl.assets.texture;

import com.akjostudios.engine.api.assets.Texture;
import com.akjostudios.engine.api.resource.asset.AssetLoader;
import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import com.akjostudios.engine.runtime.util.OpenGLUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;

public final class TextureLoader implements AssetLoader<Texture, TextureLoader.Data> {
    public record Data(
            @NotNull ByteBuffer pixels,
            int width, int height,
            int channels
    ) {}

    @Override
    public @NotNull Data loadRaw(@NotNull ResourcePath path, @NotNull FileSystem fs) throws Exception {
        try (SeekableByteChannel channel = fs.open(path)) {
            ByteBuffer fileBytes = ByteBuffer.allocateDirect((int) channel.size());
            channel.read(fileBytes);
            fileBytes.flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                ByteBuffer image = STBImage.stbi_load_from_memory(fileBytes, width, height, channels, 4);
                if (image == null) {
                    throw new IOException("❗ Failed to load texture! Reason: " + STBImage.stbi_failure_reason());
                }

                return new Data(image, width.get(0), height.get(0), channels.get(0));
            }
        }
    }

    @Override
    public @NotNull Texture createAsset(@NotNull ResourcePath path, @NotNull TextureLoader.Data data) {
        OpenGLUtil.checkContext("create texture at \"" + path + "\"");

        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                data.width, data.height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.pixels()
        );

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        STBImage.stbi_image_free(data.pixels());

        return new TextureImpl(path, id, data.width(), data.height());
    }
}