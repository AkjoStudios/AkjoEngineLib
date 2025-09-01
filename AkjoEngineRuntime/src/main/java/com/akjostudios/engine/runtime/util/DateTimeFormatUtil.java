package com.akjostudios.engine.runtime.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeFormatUtil {
    public static @NotNull String safeLocalizedPattern(@NotNull Locale locale) {
        String p = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                FormatStyle.MEDIUM, FormatStyle.FULL, IsoChronology.INSTANCE, locale
        );
        p = p.replace(",", "");
        p = p.replace("z", "").replace("zz", "").replace("zzz", "");
        p = p.replace("}", "\\}");
        p = p.replaceAll("\\s+", " ").trim();
        return addMillisAfterSeconds(p);
    }

    private static @NotNull String addMillisAfterSeconds(@NotNull String p) {
        int i = p.lastIndexOf('s');
        if (i >= 0 && !p.contains("S")) {
            int j = i + 1;
            while (j < p.length() && p.charAt(j) == 's') j++;
            return p.substring(0, j) + ".SSS" + p.substring(j);
        }
        if (!p.contains("S")) return p + " .SSS";
        return p;
    }
}