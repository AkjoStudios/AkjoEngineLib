package com.akjostudios.engine.api.assets.texture;

import com.akjostudios.engine.api.render.IRenderResolution;

public record TextureResolution(int width, int height) implements IRenderResolution {}