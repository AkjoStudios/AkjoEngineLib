package com.akjostudios.engine.res;

import com.akjostudios.engine.api.assets.Shader;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
@SuppressWarnings("unused")
public enum ShaderResources implements EngineResources<Shader> {
    UI_DEFAULT(new ResourcePath(ResourcePath.Scheme.CLASSPATH, "engine:shaders/ui_default"), Shader.class);

    private final ResourcePath path;
    private final Class<Shader> type;
}