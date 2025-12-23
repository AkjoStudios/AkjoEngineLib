package com.akjostudios.engine.runtime.impl.render.backend;

import com.akjostudios.engine.runtime.impl.render.commands.ClearCommand;
import com.akjostudios.engine.runtime.impl.render.commands.RenderCommand;
import org.jetbrains.annotations.NotNull;

public final class CanvasRenderBackend implements RenderBackend {
    private ClearCommand lastClear;

    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void execute(@NotNull RenderCommand command) {
        switch (command) {
            case ClearCommand clear -> lastClear = clear;
            default -> command.execute();
        }
    }

    @Override
    public void flush() {
        if (lastClear != null) {
            lastClear.execute();
            lastClear = null;
        }
    }
}