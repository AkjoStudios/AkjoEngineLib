package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.resolution.IResolution;
import com.akjostudios.engine.api.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowResolution(int width, int height) implements IResolution {
    public WindowResolution {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("❗ Window resolution must be positive.");
        }
    }

    // === 4:3 Standards ===
    public static WindowResolution QVGA = new WindowResolution(320, 240);
    public static WindowResolution SIF_PAL = new WindowResolution(384, 288);
    public static WindowResolution VGA = new WindowResolution(640, 480);
    public static WindowResolution PAL = new WindowResolution(768, 576);
    public static WindowResolution SVGA = new WindowResolution(800, 600);
    public static WindowResolution XGA = new WindowResolution(1024, 768);
    public static WindowResolution XGA_PLUS = new WindowResolution(1152, 864);
    public static WindowResolution QUAD_VGA = new WindowResolution(1280, 960);
    public static WindowResolution SXGA_PLUS = new WindowResolution(1400, 1050);
    public static WindowResolution UXGA = new WindowResolution(1600, 1200);
    public static WindowResolution QXGA = new WindowResolution(2048, 1536);
    public static WindowResolution QUXGA = new WindowResolution(3200, 2400);

    // === 16:9 Standards ===
    public static WindowResolution FWVGA = new WindowResolution(854, 480);
    public static WindowResolution WSVGA_PAL = new WindowResolution(1024, 576);
    public static WindowResolution HD = new WindowResolution(1280, 720);
    public static WindowResolution FWXGA = new WindowResolution(1366, 768);
    public static WindowResolution WSXGA = new WindowResolution(1600, 900);
    public static WindowResolution FHD = new WindowResolution(1920, 1080);
    public static WindowResolution QHD = new WindowResolution(2560, 1440);
    public static WindowResolution UHD_4K = new WindowResolution(3840, 2160);
    public static WindowResolution UHD_5K = new WindowResolution(5120, 2880);
    public static WindowResolution UHD_8K = new WindowResolution(7680, 4320);

    // === 16:10 Standards ===
    public static WindowResolution CGA = new WindowResolution(320, 200);
    public static WindowResolution WXGA = new WindowResolution(1280, 800);
    public static WindowResolution WXGA_PLUS = new WindowResolution(1440, 900);
    public static WindowResolution WSGXA_PLUS = new WindowResolution(1680, 1050);
    public static WindowResolution WUXGA = new WindowResolution(1920, 1200);
    public static WindowResolution WQXGA = new WindowResolution(2560, 1600);
    public static WindowResolution WQUXGA = new WindowResolution(3840, 2400);

    // === 21:9 Standards ===
    public static WindowResolution UWFHD = new WindowResolution(2560, 1080);
    public static WindowResolution UWQHD = new WindowResolution(3440, 1440);
    public static WindowResolution UW_5K = new WindowResolution(5120, 2160);

    // === Full Format Standards ===
    public static WindowResolution DCI_2K = new WindowResolution(2048, 1080);
    public static WindowResolution DCI_4K = new WindowResolution(4096, 2160);
    public static WindowResolution DCI_8K = new WindowResolution(8192, 4320);

    // === 5:4 Standards ===
    public static WindowResolution SXGA = new WindowResolution(1280, 1024);
    public static WindowResolution QSXGA = new WindowResolution(2560, 2048);

    // === 5:3 Standards ===
    public static WindowResolution WVGA = new WindowResolution(800, 480);
    public static WindowResolution WWXGA = new WindowResolution(1280, 768);

    // === Other Standards ===
    public static WindowResolution CIF = new WindowResolution(352, 288);
    public static WindowResolution HVGA = new WindowResolution(480, 320);
    public static WindowResolution WSVGA = new WindowResolution(1024, 600);
    public static WindowResolution SQFHD = new WindowResolution(1920, 1920);
    public static WindowResolution WQSXGA = new WindowResolution(3200, 2048);

    public static @NotNull WindowResolution DEFAULT(@NotNull Monitor monitor) {
        return new WindowResolution(
                monitor.resolution().width() / 2,
                monitor.resolution().height() / 2
        );
    }

    public static @NotNull WindowResolution FULLSCREEN(@NotNull Monitor monitor) {
        return new WindowResolution(
                monitor.resolution().width(),
                monitor.resolution().height()
        );
    }

    @Override
    public @NotNull String toString() { return "WindowResolution(" + width + "x" + height + ")"; }
}