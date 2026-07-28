package com.finanzas.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades de redondeo compartidas por la calculadora del plan.
 * Los importes se redondean a 2 decimales y los ratios a 6.
 */
public final class Money {

    public static final int AMOUNT_SCALE = 2;
    public static final int RATIO_SCALE = 6;

    private Money() {
    }

    public static BigDecimal amount(BigDecimal value) {
        return nullSafe(value).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal ratio(BigDecimal value) {
        return nullSafe(value).setScale(RATIO_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * División protegida: devuelve cero cuando el divisor es cero o nulo,
     * igual que los {@code IF(...>0, ..., 0)} del Excel original.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        BigDecimal safeDivisor = nullSafe(divisor);
        if (safeDivisor.signum() == 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return nullSafe(dividend).divide(safeDivisor, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal atLeastZero(BigDecimal value) {
        BigDecimal safe = nullSafe(value);
        return safe.signum() < 0 ? BigDecimal.ZERO.setScale(safe.scale()) : safe;
    }

    public static BigDecimal min(BigDecimal left, BigDecimal right) {
        return nullSafe(left).min(nullSafe(right));
    }
}
