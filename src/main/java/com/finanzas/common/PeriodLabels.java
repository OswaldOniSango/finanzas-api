package com.finanzas.common;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Etiquetas legibles de un mes, en español rioplatense.
 */
public final class PeriodLabels {

    private static final Locale LOCALE = Locale.forLanguageTag("es-AR");

    private PeriodLabels() {
    }

    /** Ejemplo: "Julio 2026". */
    public static String full(YearMonth yearMonth) {
        String month = yearMonth.getMonth().getDisplayName(TextStyle.FULL, LOCALE);
        return capitalize(month) + " " + yearMonth.getYear();
    }

    /** Ejemplo: "jul 26", pensado para ejes de gráficos. */
    public static String shortLabel(YearMonth yearMonth) {
        String month = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, LOCALE).replace(".", "");
        return month + " " + String.format("%02d", yearMonth.getYear() % 100);
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(LOCALE) + value.substring(1);
    }
}
