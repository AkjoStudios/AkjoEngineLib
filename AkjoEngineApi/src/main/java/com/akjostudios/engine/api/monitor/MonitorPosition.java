package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.position.IPosition2D;
import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record MonitorPosition(@NotNull Monitor monitor, long x, long y) implements IPosition2D {
    public MonitorPosition {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("❗ Monitor position must be positive.");
        }
    }

    public static @NotNull MonitorPosition TOP_LEFT(@NotNull Monitor monitor, @NotNull IResolution object) {
        return new MonitorPosition(monitor, 1, 1);
    }

    public static @NotNull MonitorPosition TOP_CENTER(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                (provider.retrieve().width() - object.width()) / 2,
                1
        );
    }

    public static @NotNull MonitorPosition TOP_RIGHT(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                provider.retrieve().width() - object.width(),
                1
        );
    }

    public static @NotNull MonitorPosition MIDDLE_LEFT(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                0,
                (provider.retrieve().height() - object.height()) / 2
        );
    }

    public static @NotNull MonitorPosition CENTER(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                (provider.retrieve().width() - object.width()) / 2,
                (provider.retrieve().height() - object.height()) / 2
        );
    }

    public static @NotNull MonitorPosition MIDDLE_RIGHT(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                provider.retrieve().width() - object.width(),
                (provider.retrieve().height() - object.height()) / 2
        );
    }

    public static @NotNull MonitorPosition BOTTOM_LEFT(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                0,
                provider.retrieve().height() - object.height()
        );
    }

    public static @NotNull MonitorPosition BOTTOM_CENTER(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                (provider.retrieve().width() - object.width()) / 2,
                provider.retrieve().height() - object.height()
        );
    }

    public static @NotNull MonitorPosition BOTTOM_RIGHT(@NotNull Monitor monitor, @NotNull IResolution object) {
        MonitorWorkArea workArea = monitor.screenArea();
        MonitorResolutionProvider provider = workArea != null ? workArea::resolution : monitor::resolution;
        return new MonitorPosition(monitor,
                provider.retrieve().width() - object.width(),
                provider.retrieve().height() - object.height()
        );
    }

    @Override
    public @NotNull String toString() {
        return "MonitorPosition(" + x + ", " + y + ")";
    }
}