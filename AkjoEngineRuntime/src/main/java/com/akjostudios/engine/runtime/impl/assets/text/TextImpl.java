package com.akjostudios.engine.runtime.impl.assets.text;

import com.akjostudios.engine.api.assets.Text;
import com.akjostudios.engine.api.resource.file.ResourcePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("ClassCanBeRecord")
public final class TextImpl implements Text {
    @Getter private final ResourcePath path;
    @Getter private final String text;

    @Override
    public void dispose() {}

    @Override
    public String toString() {
        return "TextImpl[" + path + "]" + "(" +
                "text=" + text() + ")";
    }
}