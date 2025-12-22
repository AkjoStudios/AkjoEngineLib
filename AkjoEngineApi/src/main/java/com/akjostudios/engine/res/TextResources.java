package com.akjostudios.engine.res;

import com.akjostudios.engine.api.assets.Text;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
@SuppressWarnings("unused")
public enum TextResources implements EngineResources<Text> {
    EXAMPLE(new ResourcePath(ResourcePath.Scheme.CLASSPATH, "engine:example.txt"), Text.class);

    private final ResourcePath path;
    private final Class<Text> type;
}