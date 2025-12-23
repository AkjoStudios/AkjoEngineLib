package com.akjostudios.engine.api.render.context;

import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.threading.Threading;
import org.jetbrains.annotations.NotNull;

public record RenderDevice(
        @NotNull Threading threading,
        @NotNull AssetManager assets,
        @NotNull Logger log
) {}