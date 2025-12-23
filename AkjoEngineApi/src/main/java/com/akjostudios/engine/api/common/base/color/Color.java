package com.akjostudios.engine.api.common.base.color;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record Color(
        float red,
        float green,
        float blue,
        float alpha
) implements IColor {
    public Color {
        if (red < 0 || red > 1) {
            throw new IllegalArgumentException("❗ Red channel of color must be between 0.0 and 1.0.");
        }
        if (green < 0 || green > 1) {
            throw new IllegalArgumentException("❗ Green channel of color must be between 0.0 and 1.0.");
        }
        if (blue < 0 || blue > 1) {
            throw new IllegalArgumentException("❗ Blue channel of color must be between 0.0 and 1.0.");
        }
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("❗ Alpha channel of color must be between 0.0 and 1.0.");
        }
    }

    public static Color TRANSPARENT = new Color(0F, 0F, 0F, 0F);

    public static Color WHITE = new Color(1F, 1F, 1F, 1F);
    public static Color SILVER = new Color(0.75F, 0.75F, 0.75F, 1F);
    public static Color GRAY = new Color(0.5F, 0.5F, 0.5F, 1F);
    public static Color BLACK = new Color(0F, 0F, 0F, 1F);
    public static Color RED = new Color(1F, 0F, 0F, 1F);
    public static Color MAROON = new Color(0.5F, 0F, 0F, 1F);
    public static Color YELLOW = new Color(1F, 1F, 0F, 1F);
    public static Color OLIVE = new Color(0.5F, 0.5F, 0F, 1F);
    public static Color LIME = new Color(0F, 1F, 0F, 1F);
    public static Color GREEN = new Color(0F, 0.5F, 0F, 1F);
    public static Color AQUA = new Color(0F, 1F, 1F, 1F);
    public static Color TEAL = new Color(0F, 0.5F, 0.5F, 1F);
    public static Color BLUE = new Color(0F, 0F, 1F, 1F);
    public static Color NAVY = new Color(0F, 0F, 0.5F, 1F);
    public static Color FUCHSIA = new Color(1F, 0F, 1F, 1F);
    public static Color PURPLE = new Color(0.5F, 0F, 0.5F, 1F);

    @Contract("_, _, _, _ -> new")
    public static @NotNull IColor rgba(float r, float g, float b, float a) {
        return new Color(r, g, b, a);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull IColor rgb(float r, float g, float b) {
        return rgba(r, g, b, 1F);
    }

    @Contract("_ -> new")
    public static @NotNull IColor gray(float v) {
        return rgba(v, v, v, 1F);
    }

    @Contract("_, _ -> new")
    public static @NotNull IColor gray(float v, float a) {
        return rgba(v, v, v, a);
    }

    @Contract("_ -> new")
    public static @NotNull IColor hex(@NotNull String hex) {
        String hexStr = hex.trim();
        if (hexStr.startsWith("#")) {
            hexStr = hex.substring(1);
        } else if (hexStr.regionMatches(true, 0, "0x", 0, 2)) {
            hexStr = hexStr.substring(2);
        }

        final int len = hexStr.length();
        int r, g, b, a;

        try {
            switch (len) {
                case 3 -> {
                    r = Integer.parseInt(hexStr.substring(0, 1), 16);
                    g = Integer.parseInt(hexStr.substring(1, 2), 16);
                    b = Integer.parseInt(hexStr.substring(2, 3), 16);
                    r = (r << 4) | r;
                    g = (g << 4) | g;
                    b = (b << 4) | b;
                    a = 0xFF;
                }
                case 4 -> {
                    r = Integer.parseInt(hexStr.substring(0, 1), 16);
                    g = Integer.parseInt(hexStr.substring(1, 2), 16);
                    b = Integer.parseInt(hexStr.substring(2, 3), 16);
                    a = Integer.parseInt(hexStr.substring(3, 4), 16);
                    r = (r << 4) | r;
                    g = (g << 4) | g;
                    b = (b << 4) | b;
                    a = (a << 4) | a;
                }
                case 6 -> {
                    r = Integer.parseInt(hexStr.substring(0, 2), 16);
                    g = Integer.parseInt(hexStr.substring(2, 4), 16);
                    b = Integer.parseInt(hexStr.substring(4, 6), 16);
                    a = 0xFF;
                }
                case 8 -> {
                    r = Integer.parseInt(hexStr.substring(0, 2), 16);
                    g = Integer.parseInt(hexStr.substring(2, 4), 16);
                    b = Integer.parseInt(hexStr.substring(4, 6), 16);
                    a = Integer.parseInt(hexStr.substring(6, 8), 16);
                }
                default -> throw new IllegalArgumentException(
                        "❗ Invalid hex color \"" + hex + "\"! Expected #RGB, #RGBA, #RRGGBB, or #RRGGBBAA."
                );
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "❗ Invalid hex color \"" + hex + "\"! Contains non-hex characters.",
                    e
            );
        }

        return rgba(r / 255F, g / 255F, b / 255F, a / 255F);
    }
}