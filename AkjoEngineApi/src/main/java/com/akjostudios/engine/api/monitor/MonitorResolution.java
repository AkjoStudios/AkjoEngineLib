package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record MonitorResolution(int width, int height) implements IResolution {
    public MonitorResolution {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("❗ Monitor resolution must be positive.");
        }
    }

    // === 4:3 Standards ===
    public static MonitorResolution QVGA = new MonitorResolution(320, 240);
    public static MonitorResolution SIF_PAL = new MonitorResolution(384, 288);
    public static MonitorResolution VGA = new MonitorResolution(640, 480);
    public static MonitorResolution PAL = new MonitorResolution(768, 576);
    public static MonitorResolution SVGA = new MonitorResolution(800, 600);
    public static MonitorResolution XGA = new MonitorResolution(1024, 768);
    public static MonitorResolution XGA_PLUS = new MonitorResolution(1152, 864);
    public static MonitorResolution QUAD_VGA = new MonitorResolution(1280, 960);
    public static MonitorResolution SXGA_PLUS = new MonitorResolution(1400, 1050);
    public static MonitorResolution UXGA = new MonitorResolution(1600, 1200);
    public static MonitorResolution QXGA = new MonitorResolution(2048, 1536);
    public static MonitorResolution QUXGA = new MonitorResolution(3200, 2400);

    // === 16:9 Standards ===
    public static MonitorResolution FWVGA = new MonitorResolution(854, 480);
    public static MonitorResolution WSVGA_PAL = new MonitorResolution(1024, 576);
    public static MonitorResolution HD = new MonitorResolution(1280, 720);
    public static MonitorResolution FWXGA = new MonitorResolution(1366, 768);
    public static MonitorResolution WSXGA = new MonitorResolution(1600, 900);
    public static MonitorResolution FHD = new MonitorResolution(1920, 1080);
    public static MonitorResolution QHD = new MonitorResolution(2560, 1440);
    public static MonitorResolution UHD_4K = new MonitorResolution(3840, 2160);
    public static MonitorResolution UHD_5K = new MonitorResolution(5120, 2880);
    public static MonitorResolution UHD_8K = new MonitorResolution(7680, 4320);

    // === 16:10 Standards ===
    public static MonitorResolution CGA = new MonitorResolution(320, 200);
    public static MonitorResolution WXGA = new MonitorResolution(1280, 800);
    public static MonitorResolution WXGA_PLUS = new MonitorResolution(1440, 900);
    public static MonitorResolution WSGXA_PLUS = new MonitorResolution(1680, 1050);
    public static MonitorResolution WUXGA = new MonitorResolution(1920, 1200);
    public static MonitorResolution WQXGA = new MonitorResolution(2560, 1600);
    public static MonitorResolution WQUXGA = new MonitorResolution(3840, 2400);

    // === 21:9 Standards ===
    public static MonitorResolution UWFHD = new MonitorResolution(2560, 1080);
    public static MonitorResolution UWQHD = new MonitorResolution(3440, 1440);
    public static MonitorResolution UW_5K = new MonitorResolution(5120, 2160);

    // === Full Format Standards ===
    public static MonitorResolution DCI_2K = new MonitorResolution(2048, 1080);
    public static MonitorResolution DCI_4K = new MonitorResolution(4096, 2160);
    public static MonitorResolution DCI_8K = new MonitorResolution(8192, 4320);

    // === 5:4 Standards ===
    public static MonitorResolution SXGA = new MonitorResolution(1280, 1024);
    public static MonitorResolution QSXGA = new MonitorResolution(2560, 2048);

    // === 5:3 Standards ===
    public static MonitorResolution WVGA = new MonitorResolution(800, 480);
    public static MonitorResolution WWXGA = new MonitorResolution(1280, 768);

    // === Other Standards ===
    public static MonitorResolution CIF = new MonitorResolution(352, 288);
    public static MonitorResolution HVGA = new MonitorResolution(480, 320);
    public static MonitorResolution WSVGA = new MonitorResolution(1024, 600);
    public static MonitorResolution SQFHD = new MonitorResolution(1920, 1920);
    public static MonitorResolution WQSXGA = new MonitorResolution(3200, 2048);

    @Override
    public @NotNull String toString() { return "MonitorResolution(" + width + "x" + height + ")"; }
}