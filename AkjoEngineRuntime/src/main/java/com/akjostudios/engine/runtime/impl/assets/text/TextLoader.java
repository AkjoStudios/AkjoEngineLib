package com.akjostudios.engine.runtime.impl.assets.text;

import com.akjostudios.engine.api.assets.Text;
import com.akjostudios.engine.api.resource.asset.AssetLoader;
import com.akjostudios.engine.api.resource.file.FileSystem;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

public final class TextLoader implements AssetLoader<Text, String> {
    @Override
    public @NotNull String loadRaw(@NotNull ResourcePath path, @NotNull FileSystem fs) throws Exception {
        try (SeekableByteChannel channel = fs.open(path)) {
            long fileSize = channel.size();
            if (fileSize > Integer.MAX_VALUE) {
                throw new IOException("Text asset is too large to load into a single String!");
            }

            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) == -1) { break; }
            }
            buffer.flip();

            return new String(buffer.array(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public @NotNull Text createAsset(@NotNull ResourcePath path, @NotNull String data) {
        return new TextImpl(path, data);
    }
}